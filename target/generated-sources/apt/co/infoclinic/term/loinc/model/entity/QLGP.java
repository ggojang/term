package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLGP is a Querydsl query type for LGP
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLGP extends EntityPathBase<LGP> {

    private static final long serialVersionUID = -291488690L;

    public static final QLGP lGP = new QLGP("lGP");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath parentLG = createString("parentLG");

    public final StringPath parentLGId = createString("parentLGId");

    public final StringPath status = createString("status");

    public QLGP(String variable) {
        super(LGP.class, forVariable(variable));
    }

    public QLGP(Path<? extends LGP> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLGP(PathMetadata<?> metadata) {
        super(LGP.class, metadata);
    }

}

