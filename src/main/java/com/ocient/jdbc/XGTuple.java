package com.ocient.jdbc;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.IntStream;


import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class XGTuple implements java.sql.Struct {

    private static final Logger LOGGER = Logger.getLogger("com.ocient.jdbc");
    private ArrayList<Object> m_elements;
    private ArrayList<String> m_types;
    private XGConnection m_conn;

    XGTuple(ArrayList<Object> elements, ArrayList<String> types, final XGConnection conn, final XGStatement stmt) {
        if(elements == null)
        {
            throw new IllegalArgumentException("tuple with null elements");
        }
        m_elements = elements;
        m_types = types;
        m_conn = conn;
    }

    @Override
    public String getSQLTypeName() throws SQLException {
        //would have to loop through children return something like TUPLE(INT,TUPLE(ARRAY(INT)))
        //would be easy to return something like TUPLE(INT,TUPLE) instead but that seems weird
        return "TUPLE";
    }

    @Override
    public Object[] getAttributes() throws SQLException {
        return getAttributes(m_conn.getTypeMap());
    }

    @Override
    public Object[] getAttributes(Map<String, Class<?>> map) throws SQLException {
        Object[] objects = new Object[m_elements.size()];
        for(int i = 0; i < m_elements.size(); i++)
        {
            //colIndex is 1 indexed
            objects[i] = getObject(i + 1);
        }
        return objects;
    }
    
    
    //Some utility methods that are on result set
    
    //colIndex is 1 indexed
    public final Object getObject(int columnIndex, Map<String, Class<?>> map)  throws SQLException {
        if (columnIndex < 1 || columnIndex > m_elements.size())
        {
            throw SQLStates.COLUMN_NOT_FOUND.clone();
        }

        Object col = m_elements.get(columnIndex - 1);

        final Class<?> clazz = map.get(m_types.get(columnIndex - 1));

		if (clazz == null)
		{
			return col;
		}

		if (clazz.getCanonicalName().equals("java.lang.String"))
		{
			return col.toString();
		}

		try
		{
			final Constructor<?> c = clazz.getConstructor(col.getClass());
			return c.newInstance(col);
		}
		catch (final Exception e)
		{
			LOGGER.log(Level.WARNING, String.format("Exception %s occurred during getObject() with message %s", e.toString(), e.getMessage()));
			throw SQLStates.newGenericException(e);
		}

    }

    //colIndex is 1 indexed
    public final Object getObject(int columnIndex)  throws SQLException {
        return getObject(columnIndex, m_conn.getTypeMap());
    }

    //colIndex is 1 indexed
    public final <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        if (columnIndex < 1 || columnIndex > m_elements.size())
        {
            throw SQLStates.COLUMN_NOT_FOUND.clone();
        }
        
        Object o = m_elements.get(columnIndex - 1);
        if (type.getCanonicalName().equals("java.lang.String"))
        {
            return (T) o.toString();
        }

        try
        {
            final Constructor<?> c = type.getConstructor(o.getClass());
            return (T) c.newInstance(o);
        }
        catch (final Exception e)
        {
            throw SQLStates.newGenericException(e);
        }
    }

    public final int size() {
        return m_elements.size();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("<<");
        boolean didOne = false;
        for(Object o : m_elements)
        {
            if(didOne)
            {
                builder.append(", ");
            }
            else
            {
                didOne = true;
            }
            
            if(o == null)
            {
                builder.append("NULL");
            }
            else if(o instanceof byte[])
            {
                builder.append("0x");
                builder.append(o);
            }
            else{
                builder.append(o);
            }
        }
        builder.append(">>");
        return builder.toString();
    }
}