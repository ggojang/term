package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QMapTo is a Querydsl query type for MapTo
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QMapTo extends EntityPathBase<MapTo> {

    private static final long serialVersionUID = -946025296L;

    public static final QMapTo mapTo1 = new QMapTo("mapTo1");

    public final StringPath code = createString("code");

    public final StringPath comment = createString("comment");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath mapTo = createString("mapTo");

    public QMapTo(String variable) {
        super(MapTo.class, forVariable(variable));
    }

    public QMapTo(Path<? extends MapTo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMapTo(PathMetadata<?> metadata) {
        super(MapTo.class, metadata);
    }

}

