/**
    This file is part of the Zeidon Java Object Engine (Zeidon JOE).

    Zeidon JOE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Zeidon JOE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Zeidon JOE.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2009-2012 QuinSoft
 */
/**
 * 
 */
package com.quinsoft.zeidon.standardoe;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.quinsoft.zeidon.Blob;
import com.quinsoft.zeidon.View;
import com.quinsoft.zeidon.WriteOiFlags;
import com.quinsoft.zeidon.ZeidonException;
import com.quinsoft.zeidon.objectdefinition.InternalType;
import com.quinsoft.zeidon.objectdefinition.ViewAttribute;
import com.quinsoft.zeidon.objectdefinition.ViewEntity;
import com.quinsoft.zeidon.utils.JoeUtils;
import com.quinsoft.zeidon.utils.PortableFileReader;

/**
 * Writes an object instance to a stream.
 *  
 * @author DG
 *
 */
class WriteOiToStream
{
    private static final long META_OI_LOCKED =   0x00000001;
    private static final long META_OI_READONLY = 0x00000002;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");

    private ViewImpl              view;
    private Writer                writer;
    private EnumSet<WriteOiFlags> flags;
    private final String          name;

    /**
     * Class to write an OI to a stream.
     * @param view View to OI
     * @param writer Stream to write to
     * @param name Description of object (usually file name)
     * @param control Write options.
     */
    WriteOiToStream(ViewImpl view, Writer writer, String name, long control )
    {
        super();
        this.view = view;
        this.writer = writer;
        this.name = name;
        
        flags = EnumSet.noneOf( WriteOiFlags.class );
        if ( ( control & View.CONTROL_INCREMENTAL ) != 0 )
            flags.add( WriteOiFlags.fINCREMENTAL );

        if ( ( control & View.CONTROL_ENTITY_KEYS ) != 0 )
            flags.add( WriteOiFlags.fENTITY_KEYS );

        if ( ( control & View.CONTROL_ENTITY_TAGS) != 0 )
            flags.add( WriteOiFlags.fENTITY_TAGS );

        if ( ( control & View.CONTROL_KEYS_ONLY ) != 0 )
            flags.add( WriteOiFlags.fKEYS_ONLY);
    }
    
    WriteOiToStream(ViewImpl view, Writer writer, String name, EnumSet<WriteOiFlags> flags )
    {
        super();
        this.view = view;
        this.flags = flags;
        this.writer = writer;
        this.name = name;
    }
    
    private void write( String buffer ) throws IOException
    {
        writer.write( buffer );
    }
    
    private void write(String control, Object...objects ) throws IOException
    {
        writer.write( JoeUtils.format( control, objects ) );
    }

    private void writeln() throws IOException
    {
        writer.write( "\n" );
    }
    
    private void writeln(String control, Object...objects ) throws IOException
    {
        write( JoeUtils.format( control, objects ) );
        writeln();
    }

    void writeToStream()
    {
        // Compile a regex that will search for special printable chars later on.
        Pattern specialChars = Pattern.compile( ".*[\\n\\r" + PortableFileReader.STRING_STORED_AS_BLOB_REGEX + "]+.*", Pattern.DOTALL );
        
        // Since we use it a lot, create a local value.
        boolean writeIncremental = flags.contains( WriteOiFlags.fINCREMENTAL ); 
        
        // Used to create the link statements at the end.
        int     hierIndex = 0;
        
        // Initialize instance flags that we'll use during the write.
        for ( EntityInstanceImpl ei : view.getObjectInstance().getEntities() )
        {
            ei.setWritten( false );
            ei.setRecordOwner( false );
            ei.setHierIndex( -1 );
        }
        
        long lastLinked = -1;

        String erDate = "0";
        String incremental = writeIncremental ? "1" : "0";
        String compressed = "0";
        String optimisticOIs = "0";
        String attribFlags = writeIncremental ? "1" : "0";

        String header = String.format( "z%s%s%s%s%sZeidon    %8s %s %s",
                                        erDate, incremental, compressed, optimisticOIs, attribFlags,
                                        name, view.getViewOd().getName().toUpperCase(),
                                        DATE_FORMATTER.print( new DateTime() ) );
        try
        {
            writeln( header );
            
            if ( writeIncremental )
            {
                long flags = 0;
                if ( view.getObjectInstance().isLocked() )
                    flags |= META_OI_LOCKED;
                
                if ( view.getObjectInstance().isReadOnly() )
                    flags |= META_OI_READONLY;
                
                writeln("mOIFLAGS    %x", flags);
            }
            
            // Loop through the entities.  We can't use the iterator because the inner loop
            // object may skip some.
            for ( EntityInstanceImpl ei = view.getObjectInstance().getRootEntityInstance();
                  ei != null;
                  ei = ei.getNextHier() )
            {
                ViewEntity viewEntity = ei.getViewEntity();
                if ( ei.isHidden() && ! writeIncremental )
                {
                    // EI is hidden and we're not writing incrementals, so skip this one
                    // and all its children.
                    ei = ei.getLastChildHier();
                    continue;
                }
        
                ei.setHierIndex( hierIndex++ );
                
                // Write out entity name and instance flags.
                write( "e%-9s %d", viewEntity.getName(), ei.getLevel() );
                if ( writeIncremental )
                {
                    // Write the incremental flags.
                    write( ",%d", ei.getInstanceFlags() );
                }
                writeln();
                
                if ( flags.contains( WriteOiFlags.fENTITY_TAGS ) || ei.getTag() != null )
                {
                    String tag = ei.getTag();
                    if ( StringUtils.isBlank( tag ) )
                        tag = Integer.toHexString( ei.hashCode() );
                    writeln( "mETAG      %s", tag );
                }
                
                if ( flags.contains( WriteOiFlags.fENTITY_KEYS ) )
                {
                    writeln( "mEKEY      %d", ei.getEntityKey() );
                }

                // If the EI has already been written (this means it's linked to another
                // EI that has already been written) and it has no non-persist record,
                // then we don't need to write it's attribute values.
                if ( ei.isWritten() )
                {
                    lastLinked = ei.getHierIndex();
                    ei.setWritten( true );
                }
                else
                {
                    // The ei is linked and it hasn't been written so it must be the record
                    // owner.
                    ei.setRecordOwner( true );
                    
                    // Set the written flag for all the linked instances that belong
                    // to this OI.
                    for ( EntityInstanceImpl linked : ei.getAllLinkedInstances() )
                    {
                        if ( linked.getObjectInstance() == view.getObjectInstance() )
                            linked.setWritten( true );
                    }
                }
                
                // Loops through all non-null attributes.
                for ( ViewAttribute viewAttrib : ei.getNonNullAttributeList() )
                {
                    // Don't bother if the attribute is derived.
                    if ( viewAttrib.isDerived() )
                        continue;
                    
                    if ( flags.contains( WriteOiFlags.fKEYS_ONLY ) && ! viewAttrib.isKey() )
                        continue;
                    
                    // If this entity is the one that was most recently flagged as linked, don't
                    // write persistent attributes -- they were already written for a linked
                    // instance.
                    if ( viewAttrib.isPersistent() && ei.getHierIndex() == lastLinked )
                        continue;

                    // Write the attribute flags if they aren't 0.
                    String flags = "";
                    if ( writeIncremental && ei.getInternalAttribute( viewAttrib ).getAttributeFlags() != 0 )
                        flags = String.format(",%x", ei.getInternalAttribute( viewAttrib ).getAttributeFlags() );

                    write("a%-9s ", viewAttrib.getName() + flags);
                    
                    if ( viewAttrib.getType() == InternalType.BLOB )
                    {
                        Blob blob = (Blob) ei.getInternalAttributeValue( viewAttrib );
                        byte[] bytes = blob.getBytes();
                        writeln("%d", bytes.length );
                        write( bytes.toString() );
                    }
                    else
                    {
                        String value = ei.getStringFromAttribute( viewAttrib );

                        // If the attribute type is a string then check to see if it contains "special"
                        // characters that interfere with normal attribute values, like "\n".
                        if ( viewAttrib.getType() == InternalType.STRING &&
                             ( value.length() > 254 || specialChars.matcher( value ).matches() ) )
                        {
                            writeln("%c%d", PortableFileReader.STRING_STORED_AS_BLOB, value.length() );
                        }
                        
                        writeln("%s", value );
                    }
                    
                } // for each attribute...
                
                // Write a blank line just to look pretty.
                writeln();

            } // for each entity instance...

            // If any intra-object linked instances were found, create
            // link records now.
            if ( lastLinked > -1 )
            {
                for ( EntityInstanceImpl ei : view.getObjectInstance().getEntities() )
                {
                    // If we've gone past the last linked EI we're done.
                    if ( ei.getHierIndex() > lastLinked )
                        break;
                    
                    // If index is -1 it wasn't written.
                    if ( ei.getHierIndex() == -1 )
                        continue;
                    
                    // If the entity is the record owner then we don't write link cards.
                    // Link records are written for the non-record owner.
                    if ( ei.isRecordOwner() )
                        continue;
                    
                    synchronized ( ei.getAllLinkedInstances() )
                    {
                        for ( EntityInstanceImpl linked : ei.getAllLinkedInstances() )
                        {
                            if ( linked == ei )
                                continue;  // Don't write a link record for ourself.
                            
                            if ( linked.getObjectInstance() == view.getObjectInstance() &&
                                 linked.isRecordOwner() )
                            {
                                assert ei.getHierIndex() != linked.getHierIndex() : "Mismatched record owners.";
                                assert ei.getViewEntity().getErEntityToken() == linked.getViewEntity().getErEntityToken() :
                                       "Mismatched entity tokens";
                                
                                writeln("i%-9d %d", ei.getHierIndex(), linked.getHierIndex() );
                                break;
                            }
                        }
                    }
                } // for each entity instance...
            } // if ( lastLinked > -1 )...
            
            // Indicate that the OI is done.
            writeln( "ZEND" );
        }
        catch ( Throwable e )
        {
            throw ZeidonException.wrapException( e );
        }
    }
}
