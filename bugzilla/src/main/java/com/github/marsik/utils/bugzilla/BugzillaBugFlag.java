package com.github.marsik.utils.bugzilla;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "flag")
public class BugzillaBugFlag {
    private final String flag;
    private final LocalDateTime modifiedAt;

    public BugzillaBugFlag(Map<String, Object> flag) {
        this.flag = (String)flag.get("name") + flag.get("status");
        modifiedAt = LocalDateTime.ofInstant(((Date)flag.get("modification_date")).toInstant(), ZoneId.of("UTC"));
    }

    public BugzillaBugFlag(final String flag) {
        this.flag = flag;
        modifiedAt = LocalDateTime.now();
    }

    /**
     * This method checks whether this flag is an approval flag for given targetRelease
     *
     * @param targetRelease
     * @return true if the flag approves the given release
     */
    public boolean approves(String targetRelease) {
        return flag.equals(targetRelease + "+"); // ovirt-X.Y.Z+
    }

    /**
     * This method checks whether a flags marks a bug as a future RFE
     * @return true if the flag is a future flag
     */
    public boolean futureFlag() {
        return flag.matches(".*-future[?+]");
    }
}
