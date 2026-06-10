package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QProperty is a Querydsl query type for Property
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QProperty extends EntityPathBase<Property> {

    private static final long serialVersionUID = -625994884L;

    public static final QProperty property = new QProperty("property");

    public final StringPath category = createString("category");

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath value = createString("value");

    public QProperty(String variable) {
        super(Property.class, forVariable(variable));
    }

    public QProperty(Path<? extends Property> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProperty(PathMetadata<?> metadata) {
        super(Property.class, metadata);
    }

}

