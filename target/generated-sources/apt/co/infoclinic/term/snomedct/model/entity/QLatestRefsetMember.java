package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QLatestRefsetMember is a Querydsl query type for LatestRefsetMember
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLatestRefsetMember extends EntityPathBase<LatestRefsetMember> {

    private static final long serialVersionUID = -2040535267L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLatestRefsetMember latestRefsetMember = new QLatestRefsetMember("latestRefsetMember");

    public final StringPath effectiveTime = createString("effectiveTime");

    public final StringPath field1Id = createString("field1Id");

    public final StringPath field1Value = createString("field1Value");

    public final StringPath field2Id = createString("field2Id");

    public final StringPath field2Value = createString("field2Value");

    public final StringPath field3Id = createString("field3Id");

    public final StringPath field3Value = createString("field3Value");

    public final StringPath field4Id = createString("field4Id");

    public final StringPath field4Value = createString("field4Value");

    public final StringPath field5Id = createString("field5Id");

    public final StringPath field5Value = createString("field5Value");

    public final StringPath field6Id = createString("field6Id");

    public final StringPath field6Value = createString("field6Value");

    public final StringPath field7Id = createString("field7Id");

    public final StringPath field7Value = createString("field7Value");

    public final QLatestRefsetMemberId id;

    public final StringPath moduleId = createString("moduleId");

    public final StringPath moduleName = createString("moduleName");

    public final BooleanPath referencedComponentActive = createBoolean("referencedComponentActive");

    public final StringPath referencedComponentId = createString("referencedComponentId");

    public final StringPath referencedComponentName = createString("referencedComponentName");

    public final StringPath refsetId = createString("refsetId");

    public final StringPath refsetName = createString("refsetName");

    public QLatestRefsetMember(String variable) {
        this(LatestRefsetMember.class, forVariable(variable), INITS);
    }

    public QLatestRefsetMember(Path<? extends LatestRefsetMember> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QLatestRefsetMember(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QLatestRefsetMember(PathMetadata<?> metadata, PathInits inits) {
        this(LatestRefsetMember.class, metadata, inits);
    }

    public QLatestRefsetMember(Class<? extends LatestRefsetMember> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QLatestRefsetMemberId(forProperty("id")) : null;
    }

}

