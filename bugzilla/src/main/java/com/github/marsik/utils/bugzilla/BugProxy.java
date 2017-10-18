package com.github.marsik.utils.bugzilla;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BugProxy {
    private final Bug bug;
    private final Set<BugzillaBugFlag> flags = new HashSet<>();

    public BugProxy(Bug bug) {
        this.bug = bug;
    }

    public Bug getBug() {
        return bug;
    }

    public String getCommunity() {
        return (String) bug.getOrDefault("classification", "");
    }

    public String getId() {
        return (String) bug.get("id").toString();
    }

    public String getSummary() {
        return (String) bug.get("summary");
    }

    public String getDescription() {
        return (String) bug.get("description");
    }

    public String getStatus() {
        return (String) bug.get("status");
    }

    public String getSeverity() {
        return (String) bug.get("severity");
    }

    public String getPriority() {
        return (String) bug.get("priority");
    }

    public List<String> getVerified() {
        return getList("cf_verified");
    }

    public String get(String key) {
        return (String) bug.get(key);
    }

    public Date getDate(String key) { return getAs(key, Date.class); }

    public Date getLastChangeTime() {
        return getDate("last_change_time");
    }

    @SuppressWarnings("unchecked")
    private <T> T getAs(String key, Class<T> cls) {
        return bug.getAs(key, cls);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getList(String key) {
        return bug.getList(key);
    }

    public String getAssignedTo() {
        return (String) bug.get("assigned_to");
    }

    public String getTargetRelease() {
        return (String) firstValue(bug.get("target_release"));
    }


    public String getTargetMilestone() {
        return (String) firstValue(bug.get("target_milestone"));
    }

    private Object firstValue(Object value) {
        if (value instanceof Object[]) {
            value = ((Object[]) value)[0];
        }

        if (value.toString().trim().equals("---")) return "";
        else return value;
    }

    @SuppressWarnings("unchecked")
    public void loadFlags(BugProxy data) {
        final List<String> flags = data.getList("flags").stream()
                .map(v -> (Arrays.asList(((String)v).split(" +"))))
                .flatMap(Collection::stream).collect(Collectors.toList());

        for (Object flag0: flags) {
            Map<String,Object> flag = (Map<String,Object>)flag0;
            this.flags.add(new BugzillaBugFlag(flag));
        }
    }

    public Set<BugzillaBugFlag> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    public List<String> getKeywords() {
        return getList("keywords").stream()
                .map(v -> (Arrays.asList(((String)v).split(" +"))))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    public List<String> getBlocks() {
        return getList("blocks").stream()
                .map(v -> (Arrays.asList((v.toString()).split(" +"))))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    public String getPmScore() {
        return (String) bug.get("cf_pm_score");
    }

    public String getWhiteBoard() {
        return (String) bug.get("whiteboard");
    }
}
