/**
 *
 */
package com.quinsoft.zeidon.webserver;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import com.quinsoft.zeidon.ObjectEngine;
import com.quinsoft.zeidon.standardoe.DefaultJavaOeConfiguration;
import com.quinsoft.zeidon.standardoe.JavaObjectEngine;
import com.quinsoft.zeidon.standardoe.JavaOeConfiguration;
import com.quinsoft.zeidon.utils.RestServerImplementation;
import com.quinsoft.zeidon.utils.RestServerImplementation.DataFormat;

/**
 * A simple RESTEasy service gateway for activating and committing OIs.
 *
 * @author dgc
 *
 */
@Path("/")
public class ResteasyGateway extends Application
{
    @Resource private JavaOeConfiguration oeConfig;
    @Resource private ObjectEngine objectEngine; // = JavaObjectEngine.getInstance();
    private final RestServerImplementation restImpl;

    public ResteasyGateway( @Context ServletContext servletContext )
    {
        if ( objectEngine == null )
        {
            if ( oeConfig == null )
            {
                String contextPath = servletContext.getContextPath();
                oeConfig = new DefaultJavaOeConfiguration().setJmxAppName( contextPath.substring( 1 ) );
            }

            objectEngine = new JavaObjectEngine( oeConfig );
        }
        
        restImpl = new RestServerImplementation( objectEngine );
    }

    @GET
    @Path("/activate")
    @Produces("text/plain")
    public String activateGet( @QueryParam("application") String applicationName,
                               @QueryParam("viewOdName")  String viewOdName )
    {
        return "<zOI></zOI>";
    }

    @POST
    @Path("/activate")
    @Produces("application/json")
    @Consumes("application/json")
    public String activatePost( @QueryParam("application") String applicationName,
                                @QueryParam("viewOdName")  String viewOdName,
                                String content )
    {
        return restImpl.activate( applicationName, viewOdName, content, DataFormat.JSON );
    }

    @GET
    @Path("/hello")
    @Produces("text/plain")
    public String hello()
    {
        return "Hello Zeidon World";
    }

    @GET
    @Path("/echo/{message}")
    @Produces("text/plain")
    public String echo( @PathParam("message") String message )
    {
        return message;
    }

    public ObjectEngine getObjectEngine()
    {
        return objectEngine;
    }

    public void setObjectEngine( ObjectEngine objectEngine )
    {
        this.objectEngine = objectEngine;
    }
}
