package com.ocient.jdbc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class TraceProcessor
{
	static HashMap<Integer, PrintWriter> outFiles = new HashMap<>();
	static int lastThreadId = 0;

	public static void main(final String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Usage: TraceProcessor <trace input file>");
			System.exit(1);
		}

		final String inFile = args[0];
		BufferedReader reader;
		try
		{
			final InputStreamReader isr = new InputStreamReader(new FileInputStream(inFile), Charset.defaultCharset());
			reader = new BufferedReader(isr);
			String line = reader.readLine();
			while (line != null)
			{
				// 09:41:31 [19] com.ocient.jdbc.XGConnection isClosed INFO: Called isClosed()
				final int start = 10;
				final int end = line.indexOf(']');
				int threadId = 0;

				if (line.length() < start || line.charAt(start - 1) != '[' || end == -1)
				{
					threadId = lastThreadId;
				}
				else
				{
					threadId = Integer.parseInt(line.substring(start, end));
				}

				PrintWriter outFile = outFiles.get(threadId);
				if (outFile == null)
				{
					FileOutputStream outputStream = new FileOutputStream("thread" + threadId + ".txt");
					outFile = new PrintWriter(new OutputStreamWriter(outputStream, Charset.defaultCharset()));
					outFiles.put(threadId, outFile);
				}

				outFile.println(line);
				line = reader.readLine();
				lastThreadId = threadId;
			}

			reader.close();

			for (final Map.Entry<Integer, PrintWriter> entry : outFiles.entrySet())
			{
				entry.getValue().close();
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
}
