package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QAbstractReferenceset is a Querydsl query type for AbstractReferenceset
 */
@Generated("com.mysema.query.codegen.SupertypeSerializer")
public class QAbstractReferenceset extends EntityPathBase<AbstractReferenceset> {

    private static final long serialVersionUID = 886742726L;

    public static final QAbstractReferenceset abstractReferenceset = new QAbstractReferenceset("abstractReferenceset");

    public final QComponent _super = new QComponent(this);

    //inherited
    public final BooleanPath active = _super.active;

    //inherited
    public final StringPath effectiveTime = _super.effectiveTime;

    public final StringPath field1 = createString("field1");

    public final StringPath field2 = createString("field2");

    public final StringPath field3 = createString("field3");

    public final StringPath field4 = createString("field4");

    public final StringPath field5 = createString("field5");

    public final StringPath field6 = createString("field6");

    public final StringPath field7 = createString("field7");

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final StringPath moduleId = _super.moduleId;

    public final StringPath referencedComponentId = createString("referencedComponentId");

    public final StringPath referencesetId = createString("referencesetId");

    public final StringPath refsetId = createString("refsetId");

    public QAbstractReferenceset(String variable) {
        super(AbstractReferenceset.class, forVariable(variable));
    }

    public QAbstractReferenceset(Path<? extends AbstractReferenceset> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAbstractReferenceset(PathMetadata<?> metadata) {
        super(AbstractReferenceset.class, metadata);
    }

}

