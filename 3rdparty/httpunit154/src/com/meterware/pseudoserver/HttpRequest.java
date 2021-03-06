package com.meterware.pseudoserver;
/********************************************************************************************************************
* $Id: HttpRequest.java,v 1.4 2003/06/24 22:39:41 russgold Exp $
*
* Copyright (c) 2001-2003, Russell Gold
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
* documentation files (the "Software"), to deal in the Software without restriction, including without limitation
* the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
* to permit persons to whom the Software is furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all copies or substantial portions
* of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
* THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
* DEALINGS IN THE SOFTWARE.
*
*******************************************************************************************************************/
import com.meterware.httpunit.HttpUnitUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;


/**
 * Represents a single HTTP request, extracted from the input stream.
 */
class HttpRequest {

    private static final int CR = 13;
    private static final int LF = 10;

    private Reader         _reader;
    private String         _protocol;
    private String         _command;
    private String         _uri;
    private Hashtable      _headers = new Hashtable();
    private Hashtable      _parameters;
    private byte[]         _requestBody;


    HttpRequest( InputStream inputStream ) throws IOException {
        StringTokenizer st = new StringTokenizer( readHeaderLine( inputStream ) );
        _command  = st.nextToken();
        _uri      = st.nextToken();
        _protocol = st.nextToken();

        if (!_command.equals( "GET" ) && !_command.equals( "POST" ) && !_command.equals( "PUT" )) {
            throw new UnknownMethodException( _command );
        }
        readHeaders( inputStream );
        readContent( inputStream );
    }


    Reader getReader() {
        return _reader;
    }

    String getCommand() {
        return _command;
    }

    String getURI() {
        return _uri;
    }


    String getProtocol() {
        return _protocol;
    }


    String getHeader( String name ) {
        return (String) _headers.get( name.toUpperCase() );
    }


    byte[] getBody() {
        return _requestBody;
    }


    /**
     * Returns the parameter with the specified name. If no such parameter exists, will
     * return null.
     **/
    String[] getParameter( String name ) {
        if (_parameters == null) {
            _parameters = readParameters( new String( _requestBody ) );
        }
        return (String[]) _parameters.get( name );
    }


    public String toString() {
        StringBuffer sb = new StringBuffer( "HttpRequest[ ");
        sb.append( _command ).append( ' ' ).append( _uri ).append( ' ' ).append( _protocol ).append( "\n" );
        for (Enumeration e = _headers.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            sb.append( "      " ).append( key ).append( ": " ).append( _headers.get( key ) ).append( "\n" );
        }
        sb.append( "   body contains " ).append( getBody().length ).append( " byte(s) ]" );
        return sb.toString();
    }


    private void readContent( InputStream inputStream ) throws IOException {
        _requestBody = new byte[ getContentLength() ];
        inputStream.read( _requestBody );
        _reader = new InputStreamReader( new ByteArrayInputStream( _requestBody ) );
    }


    private int getContentLength() {
        try {
            return Integer.parseInt( getHeader( "Content-Length" ) );
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    private void readHeaders( InputStream inputStream ) throws IOException {
        String lastHeader = null;

        String header = readHeaderLine( inputStream );
        while (header.length() > 0) {
    	    if (header.charAt(0) <= ' ') {
    	        if (lastHeader == null) continue;
    		    _headers.put( lastHeader, _headers.get( lastHeader ) + header.trim() );
    	    } else {
    	        lastHeader = header.substring( 0, header.indexOf(':') ).toUpperCase();
                _headers.put( lastHeader, header.substring( header.indexOf(':')+1 ).trim() );
    	    }
            header = readHeaderLine( inputStream );
        }
    }


    private String readHeaderLine( InputStream inputStream ) throws IOException {
        StringBuffer sb = new StringBuffer();
        int b = inputStream.read();
        while (b != CR) {
            sb.append( (char) b );
            b = inputStream.read();
        }

        b = inputStream.read();
        if (b != LF) throw new IOException( "Bad header line termination: " + b );

        return sb.toString();
    }


    private Hashtable readParameters( String content ) {
        Hashtable parameters = new Hashtable();
	    if (content == null || content.trim().length() == 0) return parameters;

        StringTokenizer st = new StringTokenizer( content, "&=" );
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            if (st.hasMoreTokens()) {
                addParameter( parameters, HttpUnitUtils.decode( name ), HttpUnitUtils.decode( st.nextToken() ) );
            }
        }
        return parameters;
    }


    private void addParameter( Hashtable parameters, String name, String value ) {
        String[] oldValues = (String[]) parameters.get( name );
        if (oldValues == null) {
            parameters.put( name, new String[] { value } );
        } else {
            String[] values = new String[ oldValues.length+1 ];
            System.arraycopy( oldValues, 0, values, 0, oldValues.length );
            values[ oldValues.length ] = value;
            parameters.put( name, values );
        }
    }


    boolean wantsKeepAlive() {
        if ("Keep-alive".equalsIgnoreCase( getConnectionHeader() )) {
            return true;
        } else if (_protocol.equals( "HTTP/1.1" )) {
            return !"Close".equalsIgnoreCase( getConnectionHeader() );
        } else {
            return false;
        }
    }


    private String getConnectionHeader() {
        return getHeader( "Connection" );
    }

}

