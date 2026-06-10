package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QComponent is a Querydsl query type for Component
 */
@Generated("com.mysema.query.codegen.SupertypeSerializer")
public class QComponent extends EntityPathBase<Component> {

    private static final long serialVersionUID = 1003422416L;

    public static final QComponent component = new QComponent("component");

    public final BooleanPath active = createBoolean("active");

    public final StringPath effectiveTime = createString("effectiveTime");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath moduleId = createString("moduleId");

    public QComponent(String variable) {
        super(Component.class, forVariable(variable));
    }

    public QComponent(Path<? extends Component> path) {
        super(path.getType(), path.getMetadata());
    }

    public QComponent(PathMetadata<?> metadata) {
        super(Component.class, metadata);
    }

}

