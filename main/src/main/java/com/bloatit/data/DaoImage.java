//
// Copyright (c) 2011 Linkeos.
//
// This file is part of Elveos.org.
// Elveos.org is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Elveos.org is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
// You should have received a copy of the GNU General Public License along
// with Elveos.org. If not, see http://www.gnu.org/licenses/.
//
package com.bloatit.data;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.bloatit.framework.exceptions.lowlevel.NonOptionalParameterException;

/**
 * Represent an image. If you only need the file associated with this image, use
 * the DaoFileMetadata object.
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DaoImage extends DaoIdentifiable {

    @Basic(optional = false)
    private int horizontalSize;

    @Basic(optional = false)
    private int verticalSize;

    @Column(length = 64)
    private String compression;

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    private DaoFileMetadata file;

    /**
     * @param horizontalSize the horizontal size in px
     * @param verticalSize the vertical size in px
     * @param compression the type of compression
     * @param file the file associated with this image
     * @return the newly created image
     * @see #DaoImage(int, int, String, DaoFileMetadata)
     */
    public static DaoImage createAndPersist(final int horizontalSize, final int verticalSize, final String compression, final DaoFileMetadata file) {
        final Session session = SessionManager.getSessionFactory().getCurrentSession();
        final DaoImage image = new DaoImage(horizontalSize, verticalSize, compression, file);
        try {
            session.save(file);
        } catch (final HibernateException e) {
            session.getTransaction().rollback();
            SessionManager.getSessionFactory().getCurrentSession().beginTransaction();
            throw e;
        }
        return image;
    }

    /**
     * Create an image.
     * 
     * @param horizontalSize is the number of pixels for the X coordinate.
     * @param verticalSize is the number of pixels for the Y coordinate.
     * @param compression is a string describing the type of compression. This
     *            parameter is optional (it can be null, or empty).
     * @param file is the file where this image is stored.
     */
    private DaoImage(final int horizontalSize, final int verticalSize, final String compression, final DaoFileMetadata file) {
        super();
        if (file == null) {
            throw new NonOptionalParameterException();
        }
        this.horizontalSize = horizontalSize;
        this.verticalSize = verticalSize;
        this.compression = compression;
        this.file = file;
    }

    /**
     * @param compression the compression to set
     */
    public void setCompression(final String compression) {
        this.compression = compression;
    }

    /**
     * @return the horizontalSize
     */
    public int getHorizontalSize() {
        return this.horizontalSize;
    }

    /**
     * @return the verticalSize
     */
    public int getVerticalSize() {
        return this.verticalSize;
    }

    /**
     * @return the compression
     */
    public String getCompression() {
        return this.compression;
    }

    /**
     * @return the file
     */
    public DaoFileMetadata getFile() {
        return this.file;
    }

    // ======================================================================
    // Visitor.
    // ======================================================================

    @Override
    public <ReturnType> ReturnType accept(final DataClassVisitor<ReturnType> visitor) {
        return visitor.visit(this);
    }

    // ======================================================================
    // For hibernate mapping
    // ======================================================================

    protected DaoImage() {
        // for hibernate.
    }

    // ======================================================================
    // equals hashcode.
    // ======================================================================

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.file == null) ? 0 : this.file.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DaoImage other = (DaoImage) obj;
        if (this.file == null) {
            if (other.file != null) {
                return false;
            }
        } else if (!this.file.equals(other.file)) {
            return false;
        }
        return true;
    }
}
