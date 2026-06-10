package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QSourceOrganization is a Querydsl query type for SourceOrganization
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QSourceOrganization extends EntityPathBase<SourceOrganization> {

    private static final long serialVersionUID = 1118497237L;

    public static final QSourceOrganization sourceOrganization = new QSourceOrganization("sourceOrganization");

    public final StringPath copyright = createString("copyright");

    public final StringPath copyrightId = createString("copyrightId");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath termsOfUse = createString("termsOfUse");

    public final StringPath url = createString("url");

    public QSourceOrganization(String variable) {
        super(SourceOrganization.class, forVariable(variable));
    }

    public QSourceOrganization(Path<? extends SourceOrganization> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSourceOrganization(PathMetadata<?> metadata) {
        super(SourceOrganization.class, metadata);
    }

}

