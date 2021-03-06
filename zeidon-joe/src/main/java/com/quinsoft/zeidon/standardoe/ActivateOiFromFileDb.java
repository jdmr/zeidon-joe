/**
 *
 */
package com.quinsoft.zeidon.standardoe;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.EnumSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;

import com.quinsoft.zeidon.ActivateFlags;
import com.quinsoft.zeidon.ActivateOptions;
import com.quinsoft.zeidon.Activator;
import com.quinsoft.zeidon.CursorResult;
import com.quinsoft.zeidon.EntityCursor;
import com.quinsoft.zeidon.Task;
import com.quinsoft.zeidon.View;
import com.quinsoft.zeidon.ZeidonException;
import com.quinsoft.zeidon.objectdefinition.ViewAttribute;
import com.quinsoft.zeidon.objectdefinition.ViewEntity;
import com.quinsoft.zeidon.objectdefinition.ViewOd;

/**
 * Activates an OI from a Zeidon file DB, which is OIs stored in a directory.
 *
 * @author dgc
 *
 */
public class ActivateOiFromFileDb implements Activator
{
    private TaskImpl        task;
    private ViewImpl        view;
    private View            qual;
    private EnumSet<ActivateFlags> control;
    private ViewOd          viewOd;
    private ActivateOptions options;
    private ViewEntity      rootViewEntity;
    private ViewAttribute   qualViewAttrib;
    private String          qualValue;
    private FileDbUtils     fileDbUtils;

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.Activator#init(com.quinsoft.zeidon.Task, com.quinsoft.zeidon.View, com.quinsoft.zeidon.ActivateOptions)
     */
    @Override
    public View init( Task task, View initialView, ActivateOptions options )
    {
        assert options != null;

        this.task = (TaskImpl) task;
        if ( initialView == null )
            view = (ViewImpl) task.activateEmptyObjectInstance( options.getViewOd() );
        else
            view = ((InternalView) initialView).getViewImpl();

        this.qual = options.getQualificationObject();
        this.options = options;
        control = options.getActivateFlags();
        viewOd = options.getViewOd();
        fileDbUtils = new FileDbUtils( options );

        return view;
    }

    /**
     * Validate that the qual object is well-formed for File DB and parse out the viewEntity,
     * viewAttrib, and qual value.
     */
    private void validateQual()
    {
        if ( qual == null )
            return;

        EntityCursor entitySpec = qual.cursor( "EntitySpec" );
        if ( entitySpec.getEntityCount() > 1 )
            throw new ZeidonException( "File DB supports qualification on the root only" );

        entitySpec.setFirst();
        String entityName = entitySpec.getAttribute( "EntityName" ).getString();
        rootViewEntity = viewOd.getViewEntity( entityName );
        if ( ! rootViewEntity.equals( viewOd.getRoot() ) )
            throw new ZeidonException( "File DB supports qualification on the root only" );

        EntityCursor qualAttrib = qual.cursor( "QualAttrib" );
        if ( qualAttrib.getEntityCount() != 1 )
            throw new ZeidonException( "File DB supports qualification on one and only one attribute." );

        entityName = qualAttrib.getAttribute( "EntityName" ).getString();
        if ( ! rootViewEntity.getName().equals( entityName ) )
            throw new ZeidonException( "File DB supports qualification on the root only" );

        String attribName = qualAttrib.getAttribute( "AttributeName" ).getString();
        if ( StringUtils.isBlank( attribName ) )
            throw new ZeidonException( "File DB requires qualification on an attribute." );

        qualViewAttrib = rootViewEntity.getAttribute( attribName );

        String oper = qualAttrib.getAttribute( "Oper" ).getString();
        if ( ! StringUtils.equals( oper, "=" ) )
            throw new ZeidonException( "File DB only supports '=' for qualification comparison" );

        qualValue = qualAttrib.getAttribute( "Value" ).getString();
        if ( StringUtils.isBlank( qualValue ) )
            throw new ZeidonException( "File DB qualification requires a comparison value" );
    }

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.Activator#activate()
     */
    @Override
    public View activate()
    {
        // If the oiServerUrl specifies a file name then use that.
        if ( fileDbUtils.urlIsFile() )
            return activateFile( fileDbUtils.getDirectoryName() );

        if ( ! StringUtils.isBlank( options.getQualificationName() ) )
            return activateByQualificationName();

        if ( qual == null )
            return activateFile( fileDbUtils.genFilename( viewOd, "ALL_DATA" ) );

        validateQual();
        if ( qualViewAttrib.isKey() )
        {
            String filename = fileDbUtils.genFilename( viewOd, fileDbUtils.genKeyQualifier( qualViewAttrib, qualValue ) );
            return activateFile( filename );
        }

        // If we get here then the qualification is on a non key and we'll have to activate
        // the OIs to find the right one.
        return performScan();
    }

    /**
     * Activates each of the files in the directory that have the same ViewOd until if
     * finds the right one.
     *
     * @return
     */
    private View performScan()
    {
        task.dblog().debug( "FileDB: performing scan of %s", fileDbUtils.getDirectoryName() );
        File dir = new File( fileDbUtils.getDirectoryName() );
        FileFilter fileFilter = new WildcardFileFilter( viewOd.getName() + "*" + fileDbUtils.getFileType().getExtension() );
        File[] files = dir.listFiles( fileFilter );
        for ( File file : files )
        {
            task.dblog().debug( "Loading %s and checking for match", file.getAbsoluteFile() );
            View v = activateFile( file.getAbsolutePath() );
            if ( v.cursor( viewOd.getRoot() ).setFirst( qualViewAttrib, qualValue ) == CursorResult.SET )
            {
                task.dblog().debug( "Got a match!" );
                return v;
            }
        }

        // We didn't find a match so return the original empty view.
        return view;
    }

    private View activateFile( final String filename )
    {
        FileInputStream inputStream = null;
        task.dblog().info( "Reading OI from %s", filename );
        try
        {
            switch ( fileDbUtils.getFileType() )
            {
                case XML:
                    inputStream = new FileInputStream( filename );
                    ActivateOiFromXmlStream loader = new ActivateOiFromXmlStream( task, inputStream, control );
                    return loader.read();

                case JSON:
                    inputStream = new FileInputStream( filename );
                    return task.activateOiFromJsonStream( inputStream, control );

                default:
                    return task.activateOiFromFile( viewOd, filename, control );
            }
        }
        catch ( Exception e )
        {
            throw ZeidonException.wrapException( e ).prependFilename( filename );
        }
        finally
        {
            IOUtils.closeQuietly( inputStream );
        }
    }

    private View activateByQualificationName()
    {
        String filename = fileDbUtils.genFilename( viewOd,  options.getQualificationName() );
        return activateFile( filename );
    }

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.Activator#activate(com.quinsoft.zeidon.objectdefinition.ViewEntity)
     */
    @Override
    public int activate( ViewEntity subobjectRootEntity )
    {
        throw new ZeidonException( "Lazy-load activates are not supported by File DB Handler" );
    }
}
