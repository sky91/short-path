package x.flyspace.shortpath;

import java.util.List;

/**
 * @author sky91 - feitiandaxia1991@163.com
 */
public class InputData {
    public String from;

    public String to;

    /**
     * km/h
     */
    public double highwaySpeed;

    /**
     * ¥/(km·t)
     */
    public double highwayPrice;

    /**
     * km/h
     */
    public double railwaySpeed;

    /**
     * ¥/(km·t)
     */
    public double railwayPrice;

    public double a;

    /**
     * t
     */
    public double load;

    public List<City> cities;

    public List<Side> sides;

    public static class City {
        public String name;

        /**
         * h/t
         */
        public double transshipTime;

        /**
         * ¥/t
         */
        public double transshipPrice;
    }

    public static class Side {
        public List<String> cities;

        /**
         * km
         */
        public double highwayDistance;

        /**
         * km
         */
        public double railwayDistance;
    }
}
