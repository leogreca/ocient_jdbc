package com.ocient.jdbc;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import java.sql.ResultSet;
import java.sql.SQLException;

public class XGTuple implements Iterable<Object> {

    private ArrayList<Object> m_elements;
    private ArrayList<String> m_types;
    private XGConnection m_conn;
    private XGStatement m_statement;

    public XGTuple(ArrayList<Object> elements, ArrayList<String> types, final XGConnection conn, final XGStatement stmt) {
        if(elements == null)
        {
            throw new IllegalArgumentException("tuple with null elements");
        }
        m_elements = elements;
        m_types = types;
        m_conn = conn;
        m_statement = stmt;
    }

    public ResultSet getResultSet() throws SQLException
	{
        final ArrayList<Object> alo = new ArrayList<>();
        final ArrayList<Object> row = new ArrayList<>();
        
        final Map<String, Integer> cols2Pos = new HashMap<>();
		final TreeMap<Integer, String> pos2Cols = new TreeMap<>();
		final Map<String, String> cols2Types = new HashMap<>();

		int i = 0;
		for (final Object o : m_elements)
		{
            row.add(o);
            //Tuples are 1 indexed, rows are 0 indexed
            cols2Pos.put(String.valueOf(i + 1), i);
            pos2Cols.put(i, String.valueOf(i + 1));
            cols2Types.put(String.valueOf(i), m_types.get(i));
            i++;
        }
        alo.add(row);

        final XGResultSet retval = new XGResultSet(m_conn, alo, m_statement);
        
		retval.setCols2Pos(cols2Pos);
		retval.setPos2Cols(pos2Cols);
		retval.setCols2Types(cols2Types);
		return retval;
	}

    public final Object getObject(int columnIndex)  throws SQLException {
        if (columnIndex < 1 || columnIndex > m_elements.size())
		{
			throw SQLStates.COLUMN_NOT_FOUND.clone();
        }

        return m_elements.get(columnIndex - 1);
    }

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