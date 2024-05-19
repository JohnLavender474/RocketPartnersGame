package com.rocketpartners.game.utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.engine.common.objects.Properties;
import com.rocketpartners.game.Constants;

import java.util.Iterator;

public class MapObjectUtils {

    public static Properties convertToProps(MapObject mapObject) {
        if (mapObject instanceof RectangleMapObject) {
            return toProps((RectangleMapObject) mapObject);
        } else if (mapObject instanceof PolygonMapObject) {
            return toProps((PolygonMapObject) mapObject);
        } else if (mapObject instanceof CircleMapObject) {
            return toProps((CircleMapObject) mapObject);
        } else if (mapObject instanceof PolylineMapObject) {
            return toProps((PolylineMapObject) mapObject);
        }
        return null;
    }

    public static Properties toProps(RectangleMapObject obj) {
        Properties props = new Properties();
        MapProperties mapProps = obj.getProperties();
        props.put(Constants.ConstKeys.NAME, obj.getName());
        props.put(Constants.ConstKeys.BOUNDS, obj.getRectangle());
        Iterator<String> keys = mapProps.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            props.put(key, mapProps.get(key));
        }
        return props;
    }

    public static Properties toProps(PolygonMapObject obj) {
        Properties props = new Properties();
        MapProperties mapProps = obj.getProperties();
        props.put(Constants.ConstKeys.NAME, obj.getName());
        props.put(Constants.ConstKeys.POLYGON, obj.getPolygon());
        Iterator<String> keys = mapProps.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            props.put(key, mapProps.get(key));
        }
        return props;
    }

    public static Properties toProps(CircleMapObject obj) {
        Properties props = new Properties();
        MapProperties mapProps = obj.getProperties();
        props.put(Constants.ConstKeys.NAME, obj.getName());
        props.put(Constants.ConstKeys.CIRCLE, obj.getCircle());
        Iterator<String> keys = mapProps.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            props.put(key, mapProps.get(key));
        }
        return props;
    }

    public static Properties toProps(PolylineMapObject obj) {
        Properties props = new Properties();
        MapProperties mapProps = obj.getProperties();
        props.put(Constants.ConstKeys.NAME, obj.getName());
        props.put(Constants.ConstKeys.LINES, obj.getPolyline());
        Iterator<String> keys = mapProps.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            props.put(key, mapProps.get(key));
        }
        return props;
    }

}
