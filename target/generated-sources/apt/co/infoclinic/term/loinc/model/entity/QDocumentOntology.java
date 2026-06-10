package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QDocumentOntology is a Querydsl query type for DocumentOntology
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QDocumentOntology extends EntityPathBase<DocumentOntology> {

    private static final long serialVersionUID = 643863089L;

    public static final QDocumentOntology documentOntology = new QDocumentOntology("documentOntology");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath partCode = createString("partCode");

    public final StringPath partName = createString("partName");

    public final NumberPath<Integer> partSeq = createNumber("partSeq", Integer.class);

    public final StringPath partTypeName = createString("partTypeName");

    public QDocumentOntology(String variable) {
        super(DocumentOntology.class, forVariable(variable));
    }

    public QDocumentOntology(Path<? extends DocumentOntology> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDocumentOntology(PathMetadata<?> metadata) {
        super(DocumentOntology.class, metadata);
    }

}

