package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLGPAttr is a Querydsl query type for LGPAttr
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLGPAttr extends EntityPathBase<LGPAttr> {

    private static final long serialVersionUID = -259214497L;

    public static final QLGPAttr lGPAttr = new QLGPAttr("lGPAttr");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath parentLGId = createString("parentLGId");

    public final StringPath type = createString("type");

    public final StringPath value = createString("value");

    public QLGPAttr(String variable) {
        super(LGPAttr.class, forVariable(variable));
    }

    public QLGPAttr(Path<? extends LGPAttr> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLGPAttr(PathMetadata<?> metadata) {
        super(LGPAttr.class, metadata);
    }

}

