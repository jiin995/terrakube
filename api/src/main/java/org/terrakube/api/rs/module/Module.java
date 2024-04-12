package org.terrakube.api.rs.module;

import com.yahoo.elide.annotation.*;
import com.yahoo.elide.core.RequestScope;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.terrakube.api.plugin.security.audit.GenericAuditFields;
import org.terrakube.api.rs.IdConverter;
import org.terrakube.api.rs.Organization;
import org.terrakube.api.rs.hooks.module.ModuleManageHook;
import org.terrakube.api.rs.ssh.Ssh;
import org.terrakube.api.rs.vcs.Vcs;

import jakarta.persistence.*;

import java.sql.Types;
import java.util.*;

@ReadPermission(expression = "team view module")
@CreatePermission(expression = "team manage module")
@UpdatePermission(expression = "team manage module OR user is a super service")
@DeletePermission(expression = "team manage module")
@LifeCycleHookBinding(operation = LifeCycleHookBinding.Operation.DELETE, phase = LifeCycleHookBinding.TransactionPhase.PRECOMMIT, hook = ModuleManageHook.class)
@Include(rootLevel = false)
@Getter
@Setter
@Entity(name = "module")
public class Module extends GenericAuditFields {
    @Id
    @JdbcTypeCode(Types.VARCHAR)
    @Convert(converter = IdConverter.class)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "provider")
    private String provider;

    @Column(name = "source")
    private String source;

    @Column(name = "tag_prefix")
    private String tagPrefix;

    @Column(name = "folder")
    private String folder;

    @Column(name = "download_quantity")
    private int downloadQuantity = 0;

    @ManyToOne
    private Organization organization;

    @Exclude
    private static final GitTagsCache gitTagsCache = new GitTagsCache();

    @Transient
    @ComputedAttribute
    public String getRegistryPath(RequestScope requestScope) {
        return organization.getName() + "/" + name + "/" + provider;
    }

    @Transient
    @ComputedAttribute
    public List<String> getVersions(RequestScope requestScope) {
        return gitTagsCache.getVersions(getRegistryPath(requestScope), this.tagPrefix, this.source, this.vcs, this.ssh);
    }

    @OneToOne
    private Vcs vcs;

    @OneToOne
    private Ssh ssh;
}
