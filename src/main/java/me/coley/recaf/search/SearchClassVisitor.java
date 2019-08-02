package me.coley.recaf.search;

import org.objectweb.asm.*;

/**
 * Visitor that adds matched results in classes to a result collector.
 *
 * @author Matt
 */
public class SearchClassVisitor extends ClassVisitor {
	private final SearchCollector collector;
	private Context.ClassContext context;

	/**
	 * @param collector
	 * 		Result collector.
	 */
	public SearchClassVisitor(SearchCollector collector) {
		super(Opcodes.ASM7);
		this.collector = collector;
	}

	/**
	 * @return Root search context.
	 */
	public Context.ClassContext getContext() {
		return context;
	}

	@Override
	public void visit(int version, int access, String name, String sig, String superName, String[] interfaces) {
		context = Context.withClass(access, name);
		collector.queries(ClassNameQuery.class)
				.forEach(q -> {
					q.match(access, name);
					collector.addMatched(null, q);
				});
		collector.queries(ClassInheritanceQuery.class)
				.forEach(q -> {
					q.match(access, name);
					collector.addMatched(null, q);
				});
	}

	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		return new SearchAnnotationVisitor(collector, context, descriptor);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int ref, TypePath typePath, String descriptor, boolean visible) {
		return new SearchAnnotationVisitor(collector, context, descriptor);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature,
								   Object value) {
		if (value instanceof String) {
			collector.queries(StringQuery.class)
					.forEach(q -> {
						q.match((String) value);
						collector.addMatched(context, q);
					});
		}
		// TODO: Value
		collector.queries(MemberDefinitionQuery.class)
				.forEach(q -> {
					q.match(access, context.getName(), name, descriptor);
					collector.addMatched(context, q);
				});
		return new SearchFieldVisitor(collector, context, access, name, descriptor);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String sig, String[] ex) {
		collector.queries(MemberDefinitionQuery.class)
				.forEach(q -> {
					q.match(access, context.getName(), name, descriptor);
					collector.addMatched(context, q);
				});
		return new SearchMethodVisitor(collector, context, access, name, descriptor);
	}
}
