package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QScheme is a Querydsl query type for Scheme
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QScheme extends EntityPathBase<Scheme> {

    private static final long serialVersionUID = -198158158L;

    public static final QScheme scheme = new QScheme("scheme");

    public final StringPath authority = createString("authority");

    public final StringPath date = createString("date");

    public final StringPath edition = createString("edition");

    public final StringPath id = createString("id");

    public final StringPath name = createString("name");

    public final StringPath version = createString("version");

    public QScheme(String variable) {
        super(Scheme.class, forVariable(variable));
    }

    public QScheme(Path<? extends Scheme> path) {
        super(path.getType(), path.getMetadata());
    }

    public QScheme(PathMetadata<?> metadata) {
        super(Scheme.class, metadata);
    }

}

