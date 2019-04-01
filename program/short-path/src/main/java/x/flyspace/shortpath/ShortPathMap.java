package x.flyspace.shortpath;

import x.flyspace.shortpath.InputData.City;
import x.flyspace.shortpath.InputData.Side;

import java.util.*;

/**
 * @author sky91 - feitiandaxia1991@163.com
 */
public class ShortPathMap {

    public final List<EdgeInfo> shortestPathEdgeList;

    public final double shortestWeight;

    private final List<String> cityNameList;

    private final Map<String, Integer> cityNameIndexMap;

    private final List<String> stationNameList;

    private final Map<String, Integer> stationNameIndexMap;

    private final ArrayList<NodeInfo> nodeInfoList;

    private final Map<NodeInfo, Integer> nodeInfoIndexMap;

    private final double a;

    public ShortPathMap(InputData inputData) {
        a = inputData.a;
        Set<String> cityNameSet = new HashSet<>();
        inputData.cities.stream().map(city -> city.name).forEach(cityNameSet::add);
        inputData.sides.stream().flatMap(side -> side.cities.stream()).forEach(cityNameSet::add);
        cityNameList = new ArrayList<>(cityNameSet);
        cityNameIndexMap = reversMap(cityNameList);

        stationNameList = Arrays.asList(REPO_NAME, CAR_STATION_NAME, TRAIN_STATION_NAME);
        stationNameIndexMap = reversMap(stationNameList);

        ArrayList<EdgeInfo> edgeInfoList = new ArrayList<>();
        for(City city : inputData.cities) {
            double transshipTime = city.transshipTime * inputData.load;
            double transshipCost = city.transshipPrice * inputData.load;
            for(int i = 1; i < stationNameList.size(); i++) {
                for(int j = 1; j < stationNameList.size(); j++) {
                    if(i != j) {
                        edgeInfoList.add(new EdgeInfo(new NodeInfo(city.name, stationNameList.get(i)),
                                                      new NodeInfo(city.name, stationNameList.get(j)),
                                                      transshipTime,
                                                      transshipCost
                        ));
                    }
                }
            }
            if(city.name.equals(inputData.from)) {
                for(int i = 1; i < stationNameList.size(); i++) {
                    edgeInfoList.add(new EdgeInfo(new NodeInfo(city.name, REPO_NAME),
                                                  new NodeInfo(city.name, stationNameList.get(i)),
                                                  transshipTime / 2,
                                                  transshipCost / 2
                    ));
                }
            }
            else if(city.name.equals(inputData.to)) {
                for(int i = 1; i < stationNameList.size(); i++) {
                    edgeInfoList.add(new EdgeInfo(new NodeInfo(city.name, stationNameList.get(i)),
                                                  new NodeInfo(city.name, REPO_NAME),
                                                  transshipTime / 2,
                                                  transshipCost / 2
                    ));
                }
            }
        }

        for(Side side : inputData.sides) {
            double highwayTime = side.highwayDistance / inputData.highwaySpeed;
            double highwayCost = side.highwayDistance * inputData.highwayPrice * inputData.load;

            double railwayTime = side.railwayDistance / inputData.railwaySpeed;
            double railwayCost = side.railwayDistance * inputData.railwayPrice * inputData.load;

            List<String> cities = side.cities;
            for(int i = 0; i < cities.size(); i++) {
                for(int j = 0; j < cities.size(); j++) {
                    if(i != j) {
                        edgeInfoList.add(new EdgeInfo(new NodeInfo(cities.get(i), CAR_STATION_NAME),
                                                      new NodeInfo(cities.get(j), CAR_STATION_NAME),
                                                      highwayTime,
                                                      highwayCost
                        ));
                        edgeInfoList.add(new EdgeInfo(new NodeInfo(cities.get(i), TRAIN_STATION_NAME),
                                                      new NodeInfo(cities.get(j), TRAIN_STATION_NAME),
                                                      railwayTime,
                                                      railwayCost
                        ));
                    }
                }
            }
        }

        HashSet<NodeInfo> nodeInfoSet = new HashSet<>();
        for(EdgeInfo edgeInfo : edgeInfoList) {
            nodeInfoSet.add(edgeInfo.from);
            nodeInfoSet.add(edgeInfo.to);
        }
        nodeInfoList = new ArrayList<>(nodeInfoSet);
        nodeInfoIndexMap = reversMap(nodeInfoList);

        double[][] dist = new double[nodeInfoList.size()][nodeInfoList.size()];
        for(int i = 0; i < dist.length; i++) {
            for(int j = 0; j < dist.length; j++) {
                dist[i][j] = Double.MAX_VALUE;
            }
        }
        for(EdgeInfo edgeInfo : edgeInfoList) {
            dist[nodeInfoIndexMap.get(edgeInfo.from)][nodeInfoIndexMap.get(edgeInfo.to)] = edgeInfo.weight;
        }

        int from = nodeInfoIndexMap.get(new NodeInfo(inputData.from, REPO_NAME));
        int to = nodeInfoIndexMap.get(new NodeInfo(inputData.to, REPO_NAME));
        FloydMap floydMap = new FloydMap(dist);
        shortestWeight = floydMap.dist[from][to];

        ArrayList<Integer> pathNodeList = new ArrayList<>();
        int[][] path = floydMap.path;
        do {
            pathNodeList.add(to);
        } while((to = path[from][to]) != from);
        pathNodeList.add(to);

        Map<Long, EdgeInfo> edgeInfoMap = new HashMap<>();
        for(EdgeInfo edgeInfo : edgeInfoList) {
            edgeInfoMap.put((long) nodeInfoIndexMap.get(edgeInfo.from) << 32 | nodeInfoIndexMap.get(edgeInfo.to), edgeInfo);
        }

        shortestPathEdgeList = new ArrayList<>(pathNodeList.size() - 1);
        for(int i = pathNodeList.size() - 1; i >= 1; i--) {
            shortestPathEdgeList.add(edgeInfoMap.get((long) pathNodeList.get(i) << 32 | pathNodeList.get(i - 1)));
        }
    }

    private <T> Map<T, Integer> reversMap(List<T> list) {
        HashMap<T, Integer> map = new HashMap<>();
        for(int i = 0; i < list.size(); i++) {
            map.put(list.get(i), i);
        }
        return map;
    }

    public class NodeInfo {
        public final int city;

        public final int station;

        public NodeInfo(int city, int station) {
            this.city = city;
            this.station = station;
        }

        public NodeInfo(String cityName, String stationName) {
            this(cityNameIndexMap.get(cityName), stationNameIndexMap.get(stationName));
        }

        @Override
        public int hashCode() {
            return Objects.hash(city, station);
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            NodeInfo nodeInfo = (NodeInfo) o;
            return city == nodeInfo.city && station == nodeInfo.station;
        }

        @Override
        public String toString() {
            return nodeName();
        }

        public String nodeName() {
            return cityNameList.get(city) + stationNameList.get(station);
        }
    }

    public class EdgeInfo {
        public final NodeInfo from;

        public final NodeInfo to;

        public final double time;

        public final double cost;

        public final double weight;

        public EdgeInfo(NodeInfo from, NodeInfo to, double time, double cost) {
            this.from = from;
            this.to = to;
            this.time = time;
            this.cost = cost;
            this.weight = weight(a, time, cost);
        }

        @Override
        public String toString() {
            return from + " ---(" + weight + ")--> " + to;
        }
    }

    private static final String REPO_NAME = "仓库　";

    private static final String CAR_STATION_NAME = "汽车站";

    private static final String TRAIN_STATION_NAME = "火车站";

    private static double weight(double a, double time, double cost) {
        return a * time + (1 - a) / 10000 * cost;
    }
}
