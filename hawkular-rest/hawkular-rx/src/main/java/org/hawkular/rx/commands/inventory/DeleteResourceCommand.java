/*
 * Copyright 2016-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.rx.commands.inventory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hawkular.rx.cdi.WithValues;
import org.hawkular.rx.commands.common.AbstractHttpCommand;
import org.hawkular.rx.commands.common.HawkularConfiguration;
import org.hawkular.rx.httpclient.HttpClient;
import org.hawkular.rx.httpclient.RestResponse;

import com.netflix.hystrix.HystrixCommandGroupKey;

import rx.Observable;
import rx.Subscriber;

/**
 * @author Jirka Kremser
 */
@WithValues
// FIXME: obsolete?
public class DeleteResourceCommand extends AbstractHttpCommand<String> {

    private String resourceId;

    private String authToken;

    private String persona;

    @Override
    protected List<ImmutablePair<Class, Consumer>> getSetters() {
        final DeleteResourceCommand self = DeleteResourceCommand.this;
        return Arrays.asList(
                new ImmutablePair<>(String.class, (x) -> self.resourceId = nullOrStr(x)),
                new ImmutablePair<>(String.class, (x) -> self.authToken = nullOrStr(x)),
                new ImmutablePair<>(String.class, (x) -> self.persona = nullOrStr(x))
        );
    }

    @Inject
    private HttpClient client;

    @Inject
    private HawkularConfiguration config;

    @Inject
    private DeleteResourceCommand(InjectionPoint ip) {
        super(HystrixCommandGroupKey.Factory.asKey("inventory-resource"));
        initialize(ip);
    }


    @Override protected Observable<String> construct() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> observer) {
                if (observer.isUnsubscribed()) {
                    return;
                }
                try {
                    RestResponse response =
                            client.delete(authToken, persona, config.URL_INVENTORY + "/test/resources/" + resourceId);

                    observer.onNext(response.getBody());
                    observer.onCompleted();
                } catch (Exception e) {
                    observer.onError(e);
                }
            }
        });
    }
}
