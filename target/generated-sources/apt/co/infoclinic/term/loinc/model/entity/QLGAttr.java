package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLGAttr is a Querydsl query type for LGAttr
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLGAttr extends EntityPathBase<LGAttr> {

    private static final long serialVersionUID = 683977043L;

    public static final QLGAttr lGAttr = new QLGAttr("lGAttr");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath LGId = createString("LGId");

    public final StringPath parentLGId = createString("parentLGId");

    public final StringPath type = createString("type");

    public final StringPath value = createString("value");

    public QLGAttr(String variable) {
        super(LGAttr.class, forVariable(variable));
    }

    public QLGAttr(Path<? extends LGAttr> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLGAttr(PathMetadata<?> metadata) {
        super(LGAttr.class, metadata);
    }

}

