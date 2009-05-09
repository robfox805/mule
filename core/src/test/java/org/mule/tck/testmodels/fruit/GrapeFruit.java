/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tck.testmodels.fruit;

/**
 * A pure bean implementation
 */
public class GrapeFruit implements Fruit
{
    private Integer segments = new Integer(10);
    private Double radius = new Double(4.34);
    private String brand = "Pirulo";
    private boolean bitten = false;

    public GrapeFruit()
    {
    }

    public GrapeFruit(Integer segments, Double radius, String brand)
    {
        this.segments = segments;
        this.radius = radius;
        this.brand = brand;
    }

    /**
     * @return
     */
    public String getBrand()
    {
        return brand;
    }

    /**
     * @return
     */
    public Integer getSegments()
    {
        return segments;
    }

    /**
     * @return
     */
    public Double getRadius()
    {
        return radius;
    }

    /**
     * @param string
     */
    public void setBrand(String string)
    {
        brand = string;
    }

    /**
     * @param integer
     */
    public void setSegments(Integer integer)
    {
        segments = integer;
    }

    /**
     * @param double1
     */
    public void setRadius(Double double1)
    {
        radius = double1;
    }


    public void bite()
    {
        bitten = true;
    }

    public boolean isBitten()
    {
        return bitten;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof GrapeFruit))
        {
            return false;
        }

        GrapeFruit that = (GrapeFruit) o;

        if (bitten != that.bitten)
        {
            return false;
        }
        if (brand != null ? !brand.equals(that.brand) : that.brand != null)
        {
            return false;
        }
        if (radius != null ? !radius.equals(that.radius) : that.radius != null)
        {
            return false;
        }
        if (segments != null ? !segments.equals(that.segments) : that.segments != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = segments != null ? segments.hashCode() : 0;
        result = 31 * result + (radius != null ? radius.hashCode() : 0);
        result = 31 * result + (brand != null ? brand.hashCode() : 0);
        result = 31 * result + (bitten ? 1 : 0);
        return result;
    }
}
