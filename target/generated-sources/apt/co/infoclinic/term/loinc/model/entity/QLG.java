package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLG is a Querydsl query type for LG
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLG extends EntityPathBase<LG> {

    private static final long serialVersionUID = 1653165122L;

    public static final QLG lG = new QLG("lG");

    public final StringPath archetype = createString("archetype");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath LG = createString("LG");

    public final StringPath LGId = createString("LGId");

    public final StringPath parentLGId = createString("parentLGId");

    public final StringPath status = createString("status");

    public final StringPath versionFirstReleased = createString("versionFirstReleased");

    public QLG(String variable) {
        super(LG.class, forVariable(variable));
    }

    public QLG(Path<? extends LG> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLG(PathMetadata<?> metadata) {
        super(LG.class, metadata);
    }

}

