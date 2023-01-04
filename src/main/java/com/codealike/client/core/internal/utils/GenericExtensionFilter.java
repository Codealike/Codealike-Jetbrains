/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.utils;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Implements the filename filter by extension.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class GenericExtensionFilter implements FilenameFilter {

    private String ext;

    /**
     * Constructor with extension.
     *
     * @param ext the filename extension to use in the filter
     */
    public GenericExtensionFilter(String ext) {
        this.ext = ext;
    }

    @Override
    public boolean accept(File dir, String name) {
        return (name.endsWith(ext));
    }
}
