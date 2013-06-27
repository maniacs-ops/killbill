/*
 * Copyright 2010-2013 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.invoice.api;

import com.ning.billing.bus.PersistentBus;
import com.ning.billing.invoice.InvoiceListener;
import com.ning.billing.invoice.InvoiceTagHandler;
import com.ning.billing.invoice.notification.NextBillingDateNotifier;
import com.ning.billing.lifecycle.LifecycleHandlerType;
import com.ning.billing.lifecycle.LifecycleHandlerType.LifecycleLevel;
import com.ning.billing.notificationq.NotificationQueueService.NoSuchNotificationQueue;
import com.ning.billing.notificationq.NotificationQueueService.NotificationQueueAlreadyExists;

import com.google.inject.Inject;

public class DefaultInvoiceService implements InvoiceService {

    public static final String INVOICE_SERVICE_NAME = "invoice-service";
    private final NextBillingDateNotifier dateNotifier;
    private final InvoiceListener invoiceListener;
    private final InvoiceTagHandler tagHandler;
    private final PersistentBus eventBus;

    @Inject
    public DefaultInvoiceService(final InvoiceListener invoiceListener, final InvoiceTagHandler tagHandler, final PersistentBus eventBus, final NextBillingDateNotifier dateNotifier) {
        this.invoiceListener = invoiceListener;
        this.tagHandler = tagHandler;
        this.eventBus = eventBus;
        this.dateNotifier = dateNotifier;
    }

    @Override
    public String getName() {
        return INVOICE_SERVICE_NAME;
    }

    @LifecycleHandlerType(LifecycleHandlerType.LifecycleLevel.INIT_SERVICE)
    public void initialize() throws NotificationQueueAlreadyExists {
        try {
            eventBus.register(invoiceListener);
            eventBus.register(tagHandler);
        } catch (PersistentBus.EventBusException e) {
            throw new RuntimeException("Unable to register to the EventBus!", e);
        }
        dateNotifier.initialize();
    }

    @LifecycleHandlerType(LifecycleLevel.START_SERVICE)
    public void start() {
        dateNotifier.start();
    }

    @LifecycleHandlerType(LifecycleLevel.STOP_SERVICE)
    public void stop() throws NoSuchNotificationQueue {
        try {
            eventBus.unregister(invoiceListener);
            eventBus.unregister(tagHandler);
        } catch (PersistentBus .EventBusException e) {
            throw new RuntimeException("Unable to unregister to the EventBus!", e);
        }
        dateNotifier.stop();
    }
}
