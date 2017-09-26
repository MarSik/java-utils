package com.github.marsik.utils.bugzilla;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BugzillaClient {
    private final URL xmlRpcUrl;
    private XmlRpcClient client;
    private AuthorizationCallback authorizationCallback;
    private String token;

    private int timeout = 30000;

    public BugzillaClient(String baseUrl) throws MalformedURLException {
        xmlRpcUrl = new URL(baseUrl + "/xmlrpc.cgi");
    }

    public void setTimeout(int millis) {
        this.timeout = millis;
    }

    private XmlRpcClient getClient() {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(xmlRpcUrl);
        config.setContentLengthOptional(true);
        config.setEnabledForExtensions(true);
        config.setReplyTimeout(timeout);

        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        //client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
        return client;
    }

    public void setAuthorizationCallback(AuthorizationCallback authorizationCallback) {
        this.authorizationCallback = authorizationCallback;
    }

    private class Call {
        private final String method;
        private final Multimap<String,Object> arguments = ArrayListMultimap.create();

        public Call(String method) {
            this.method = method;
        }

        public Call argument(String key, Object value) {
            arguments.put(key, value);
            return this;
        }

        public Call arguments(Multimap<String, Object> values) {
            arguments.putAll(values);
            return this;
        }

        public CallDictResult call() {
            Map<String, Object> flatArgs = new HashMap<>();
            for (String key: arguments.keySet()) {
                Collection<Object> values = arguments.get(key);
                if (values.size() == 1) {
                    flatArgs.put(key, values.iterator().next());
                } else {
                    flatArgs.put(key, values);
                }
            }

            if (token != null) {
                flatArgs.put("Bugzilla_token", token);
            }

            Object[] callArgs = new Object[] {flatArgs};
            log.info("Calling bugzilla method {} with args {}", method, callArgs);
            try {
                return new CallDictResult((Map<String,Object>)client.execute(method, callArgs));
            } catch (XmlRpcException e) {
                log.error("Bugzilla RPC call failed: {}", e);
                return new CallDictResult(Collections.emptyMap());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public boolean open() {
        client = getClient();

        if (authorizationCallback != null) {
            CallDictResult ret = new Call("User.login")
                    .argument("login", authorizationCallback.getName())
                    .argument("password", authorizationCallback.getPassword())
                    .call();
            token = ret.getAs("token", String.class);
        }

        return true;
    }

    public void close() {
        token = null;
        client = null;
    }

    public boolean isLoggedIn() {
        return client != null
                && token != null;
    }

    public String getBugzillaVersion() {
        checkLoggedIn();
        return new Call("Bugzilla.version").call().getAs("version", String.class);
    }

    private void checkLoggedIn() {
        if (!isLoggedIn()) {
            log.error("Not logged into {}", xmlRpcUrl);
            throw new IllegalArgumentException("Not logged in.");
        }
    }

    @SuppressWarnings("unchecked")
    public Iterable<BugProxy> searchBugs(Multimap<String, Object> params) {
        checkLoggedIn();
        CallDictResult ret = new Call("Bug.search")
                .arguments(params)
                .call();

        Collection<Map<String,Object>> bugs = ret.getList("bugs")
                .stream().map(o -> (Map<String, Object>)o).collect(Collectors.toList());

        return bugs.stream().map(BugProxy::new).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public Iterable<BugProxy> getBugs(Collection<String> ids) {
        checkLoggedIn();
        CallDictResult ret = new Call("Bug.get")
                .argument("ids", new ArrayList<>(ids))
                .argument("permissive", true)
                .call();

        Collection<Map<String,Object>> bugs = ret.getList("bugs")
                .stream().map(o -> (Map<String, Object>)o).collect(Collectors.toList());

        return bugs.stream().map(BugProxy::new).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public Iterable<BugProxy> getExtra(Collection<String> ids) {
        checkLoggedIn();
        CallDictResult ret = new Call("Bug.get")
                .argument("ids", new ArrayList<>(ids))
                .argument("permissive", true)

                .argument("include_fields", "id")
                .argument("include_fields", "flags")
                .argument("include_fields", "external_bugs")
                .call();

        Collection<Map<String,Object>> bugs = ret.getList("bugs")
                .stream().map(o -> (Map<String, Object>)o).collect(Collectors.toList());

        return bugs.stream().map(BugProxy::new).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<CallDictResult> getComments(Collection<String> bzIds, Instant since) {
        checkLoggedIn();
        Call call = new Call("Bug.comments")
                .argument("ids", new ArrayList<>(bzIds))
                .argument("permissive", true);

        if (since != null) {
            call = call.argument("new_since", Date.from(since));
        }

        CallDictResult ret = call.call();

        CallDictResult bugs = ret.get("bugs");
        List<CallDictResult> comments = new ArrayList<>();

        return bugs.keySet().stream()
                .map(k -> bugs.get(k).getDictList("comments"))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public Iterable<CallDictResult> getHistory(Collection<String> bzIds) {
        checkLoggedIn();
        CallDictResult ret = new Call("Bug.history")
                .argument("ids", new ArrayList<>(bzIds))
                .argument("permissive", true)
                .call();

        return ret.getDictList("bugs");
    }
}
