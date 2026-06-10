package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLGTerm is a Querydsl query type for LGTerm
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLGTerm extends EntityPathBase<LGTerm> {

    private static final long serialVersionUID = 684528590L;

    public static final QLGTerm lGTerm = new QLGTerm("lGTerm");

    public final StringPath archetype = createString("archetype");

    public final StringPath category = createString("category");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath LGId = createString("LGId");

    public final StringPath LGIdName = createString("LGIdName");

    public final StringPath LoincNumber = createString("LoincNumber");

    public final StringPath LongCommonName = createString("LongCommonName");

    public QLGTerm(String variable) {
        super(LGTerm.class, forVariable(variable));
    }

    public QLGTerm(Path<? extends LGTerm> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLGTerm(PathMetadata<?> metadata) {
        super(LGTerm.class, metadata);
    }

}

