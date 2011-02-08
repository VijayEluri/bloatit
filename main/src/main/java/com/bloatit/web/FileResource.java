/*
 * Copyright (C) 2010 BloatIt. This file is part of BloatIt. BloatIt is free software: you
 * can redistribute it and/or modify it under the terms of the GNU Affero General Public
 * License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version. BloatIt is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details. You should have received a copy of the GNU Affero General
 * Public License along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.web;

import com.bloatit.framework.webserver.annotations.Message.Level;
import com.bloatit.framework.webserver.annotations.ParamConstraint;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.annotations.RequestParam;
import com.bloatit.framework.webserver.annotations.tr;
import com.bloatit.framework.webserver.masters.Resource;
import com.bloatit.model.FileMetadata;
import com.bloatit.web.url.FileResourceUrl;

/**
 * A response to a form used to create a new idea
 */
@ParamContainer("resource")
public final class FileResource extends Resource {

    public static final String FILE_FIELD_NAME = "id";

    @ParamConstraint(optionalErrorMsg=@tr("The id of the resource is incorrect or missing"))
    @RequestParam(name = FILE_FIELD_NAME, level = Level.ERROR)
    private final FileMetadata file;

    private final FileResourceUrl url;

    public FileResource(final FileResourceUrl url) {
        this.url = url;
        this.file = url.getFile();
    }

    @Override
    public String getFileUrl() {
        return file.getFilePath();
    }

    @Override
    public long getFileSize() {
        return file.getSize();
    }
}