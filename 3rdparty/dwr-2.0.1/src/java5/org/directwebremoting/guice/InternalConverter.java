/*
 * Copyright 2007 Tim Peierls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.directwebremoting.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.directwebremoting.convert.BaseV20Converter;
import org.directwebremoting.extend.Converter;
import org.directwebremoting.extend.ConverterManager;
import org.directwebremoting.extend.InboundContext;
import org.directwebremoting.extend.InboundVariable;
import org.directwebremoting.extend.MarshallException;
import org.directwebremoting.extend.OutboundContext;
import org.directwebremoting.extend.OutboundVariable;

/**
 * Specialized converter implementation that uses a Provider to
 * look up instances to delegate to. This class is used by 
 * {@link InternalConverterManager}.
 * @author Tim Peierls [tim at peierls dot net]
 */
class InternalConverter extends BaseV20Converter implements Converter
{
    /**
     * Only used to satisfy bindings for the two-arg {@code bindConversion}
     * method of {@link AbstractDwrModule}.
     */
    @Inject InternalConverter()
    {
        this.type = null;
        this.provider = null;
    }
    
    /**
     * Adapts a Provider into a Converter.
     */
    InternalConverter(Class<?> type, Provider<Converter> provider)
    {
        this.type = type;
        this.provider = provider;
    }

    public Object convertInbound(Class paramType, InboundVariable data, InboundContext inctx)
        throws MarshallException
    {
        try
        {
            return provider.get().convertInbound(type.asSubclass(paramType), data, inctx);
        }
        catch (ClassCastException e)
        {
            throw new MarshallException(type, e);
        }
    }
    
    public OutboundVariable convertOutbound(Object data, OutboundContext outctx)
        throws MarshallException
    {
        try
        {
            return provider.get().convertOutbound(type.cast(data), outctx);
        }
        catch (ClassCastException e)
        {
            throw new MarshallException(type, e);
        }
    }
    
    public void setConverterManager(ConverterManager mgr)
    {
        provider.get().setConverterManager(mgr);
    }
    
    private final Class<?> type;
    private final Provider<Converter> provider;
}
