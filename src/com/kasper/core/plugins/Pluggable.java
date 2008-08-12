package com.kasper.core.plugins;

public interface Pluggable {
    public int getType();
    public boolean register();
    public boolean unregister();
}
