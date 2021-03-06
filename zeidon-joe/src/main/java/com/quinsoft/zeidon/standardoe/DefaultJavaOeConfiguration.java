/**
 *
 */
package com.quinsoft.zeidon.standardoe;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.quinsoft.zeidon.ObjectEngineEventListener;
import com.quinsoft.zeidon.Task;
import com.quinsoft.zeidon.TaskLogger;
import com.quinsoft.zeidon.ZeidonLogger;
import com.quinsoft.zeidon.config.DefaultPreferencesFactory;
import com.quinsoft.zeidon.config.HomeDirectory;
import com.quinsoft.zeidon.config.HomeDirectoryFromEnvVar;
import com.quinsoft.zeidon.config.UuidGenerator;
import com.quinsoft.zeidon.config.ZeidonIniPreferences;
import com.quinsoft.zeidon.config.ZeidonPreferencesFactory;
import com.quinsoft.zeidon.domains.DomainClassLoader;
import com.quinsoft.zeidon.jmx.JmxObjectEngineMonitor;

/**
 * Returns the default configuration options for the Java Object Engine.  This is designed to be
 * easily subclassed.
 *
 * @author dg
 *
 */
public class DefaultJavaOeConfiguration implements JavaOeConfiguration
{

    protected HomeDirectory homeDirectory;
    protected DomainClassLoader domainClassLoader;
    protected ZeidonLogger zeidonLogger;
    protected ZeidonPreferencesFactory zeidonPreferencesFactory;
    protected ObjectEngineEventListener oeListener;
    protected ExecutorService activatePoolThread;
    protected UuidGenerator uuidGenerator;
    protected ConcurrentMap<String, Task> taskCacheMap;
    protected String jmxAppName;

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.standardoe.JavaOeOptions#getHomeDirectory()
     */
    @Override
    public HomeDirectory getHomeDirectory()
    {
        if ( homeDirectory == null )
            homeDirectory = new HomeDirectoryFromEnvVar();

        return homeDirectory;
    }

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.standardoe.JavaOeOptions#getDomainClassLoader()
     */
    @Override
    public DomainClassLoader getDomainClassLoader()
    {
        if ( domainClassLoader == null )
            domainClassLoader = new DomainLoader();

        return domainClassLoader;
    }

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.standardoe.JavaOeOptions#getZeidonLogger()
     */
    @Override
    public ZeidonLogger getZeidonLogger()
    {
        if ( zeidonLogger == null )
            zeidonLogger = new TaskLogger( "[system] " );

        return zeidonLogger;
    }

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.standardoe.JavaOeOptions#getPreferencesFactory()
     */
    @Override
    public ZeidonPreferencesFactory getPreferencesFactory()
    {
        if ( zeidonPreferencesFactory == null )
        {
            ZeidonIniPreferences iniPref = new ZeidonIniPreferences( getHomeDirectory(), getJmxAppName() );
            zeidonPreferencesFactory = new DefaultPreferencesFactory( iniPref, getJmxAppName() );
        }

        return zeidonPreferencesFactory;
    }

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.standardoe.JavaOeOptions#getObjectEngineListener()
     */
    @Override
    public ObjectEngineEventListener getObjectEngineListener()
    {
        if ( oeListener == null )
            oeListener = new JmxObjectEngineMonitor( this );

        return oeListener;
    }

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.standardoe.JavaOeOptions#getActivateThreadPool()
     */
    @Override
    public ExecutorService getActivateThreadPool()
    {
        if ( activatePoolThread == null )
            activatePoolThread = Executors.newCachedThreadPool();

        return activatePoolThread;
    }

    public DefaultJavaOeConfiguration setHomeDirectory( HomeDirectory homeDirectory )
    {
        this.homeDirectory = homeDirectory;
        return this;
    }

    public DefaultJavaOeConfiguration setDomainClassLoader( DomainClassLoader domainClassLoader )
    {
        this.domainClassLoader = domainClassLoader;
        return this;
    }

    public DefaultJavaOeConfiguration setZeidonLogger( ZeidonLogger zeidonLogger )
    {
        this.zeidonLogger = zeidonLogger;
        return this;
    }

    public DefaultJavaOeConfiguration setZeidonPreferencesFactory( ZeidonPreferencesFactory zeidonPreferences )
    {
        this.zeidonPreferencesFactory = zeidonPreferences;
        return this;
    }

    public DefaultJavaOeConfiguration setOeListener( ObjectEngineEventListener oeListener )
    {
        this.oeListener = oeListener;
        return this;
    }

    public DefaultJavaOeConfiguration setActivatePoolThread( ExecutorService activatePoolThread )
    {
        this.activatePoolThread = activatePoolThread;
        return this;
    }

    @Override
    public UuidGenerator getUuidGenerator()
    {
        if ( uuidGenerator == null )
            uuidGenerator = new UuidGeneratorImpl();

        return uuidGenerator;
    }

    public DefaultJavaOeConfiguration setUuidGenerator( UuidGenerator uuidGenerator )
    {
        this.uuidGenerator = uuidGenerator;
        return this;
    }

    private static class UuidGeneratorImpl implements UuidGenerator
    {
        private final TimeBasedGenerator generator;

        private UuidGeneratorImpl()
        {
            EthernetAddress nic = EthernetAddress.fromInterface();
            generator = Generators.timeBasedGenerator(nic);
        }

        @Override
        public UUID generate()
        {
            return generator.generate();
        }
    }

    @Override
    public ConcurrentMap<String, Task> getPersistentTaskCacheMap()
    {
        Cache<String, Task> x;
        if ( taskCacheMap == null )
        {
            x = CacheBuilder.newBuilder().concurrencyLevel( 10 ).build();
            taskCacheMap = x.asMap();
        }

        return taskCacheMap;
    }

    public DefaultJavaOeConfiguration setTaskCacheMap( ConcurrentMap<String, Task> taskCacheMap )
    {
        this.taskCacheMap = taskCacheMap;
        return this;
    }

    @Override
    public String getJmxAppName()
    {
        return jmxAppName;
    }

    public DefaultJavaOeConfiguration setJmxAppName( String jmxAppName )
    {
        this.jmxAppName = jmxAppName;
        return this;
    }
}
