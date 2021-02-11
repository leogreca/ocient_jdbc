package com.ocient.jdbc;

import java.util.ArrayList;
import java.util.Iterator;
import java.lang.reflect.Constructor;
import java.sql.SQLException;

public class XGTuple implements Iterable<Object> {

    private ArrayList<Object> m_elements;

    public XGTuple(ArrayList<Object> elements) {
        if(elements == null)
        {
            throw new IllegalArgumentException("tuple with null elements");
        }
        m_elements = elements;
    }

    public XGTuple(XGTuple toCopy) {
        if(toCopy == null || toCopy.m_elements == null)
        {
            throw new IllegalArgumentException("tuple with null elements");
        }
        m_elements = toCopy.m_elements;
    }

    public final Object get(int columnIndex)  throws SQLException {
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
    public Iterator<Object> iterator() {
        return m_elements.iterator();
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