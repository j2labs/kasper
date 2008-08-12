package com.kasper.core.plugins;

import com.kasper.core.exceptions.NonPluggableException;

public abstract class Outlet {
    private int _type;
    
    public int getType() { return _type; }
    
    public void registerPluggable( Pluggable plugin )
        throws NonPluggableException
    {
        int _pluginType = plugin.getType();
        if( _pluginType != _type ) {
            throw new NonPluggableException( NonPluggableException.WRONG_TYPE );
        }
    }
    
    abstract public boolean unregisterPluggable( int pluggableId );
}
