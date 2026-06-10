package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLatestRefsetMemberId is a Querydsl query type for LatestRefsetMemberId
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QLatestRefsetMemberId extends BeanPath<LatestRefsetMemberId> {

    private static final long serialVersionUID = 1845665048L;

    public static final QLatestRefsetMemberId latestRefsetMemberId = new QLatestRefsetMemberId("latestRefsetMemberId");

    public final StringPath uuid = createString("uuid");

    public final StringPath version = createString("version");

    public QLatestRefsetMemberId(String variable) {
        super(LatestRefsetMemberId.class, forVariable(variable));
    }

    public QLatestRefsetMemberId(Path<? extends LatestRefsetMemberId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLatestRefsetMemberId(PathMetadata<?> metadata) {
        super(LatestRefsetMemberId.class, metadata);
    }

}

