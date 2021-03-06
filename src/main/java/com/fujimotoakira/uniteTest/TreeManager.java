package com.fujimotoakira.uniteTest;

import com.fujimotoakira.uniteTest.io.FileEntity;
import com.fujimotoakira.uniteTest.io.TestCollection;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class TreeManager {

    private String path;
    private List<FileEntity> fileEntities = new ArrayList<>();
    private Map<String, List<ASTNode>> testMethodsMap = new LinkedHashMap<>();
    private List<ImportDeclaration> importList = new ArrayList<>();

    public TreeManager(String path) throws IOException {
        this.path = path;

        final List<Path> testsPath = TestCollection.getTestsPath(path);
        collectTests(testsPath);
    }

    public TreeManager(String path, String[] keywords) throws IOException {
        this.path = path;

        final List<Path> testsPath = TestCollection.getTestsPath(path, keywords);
        collectTests(testsPath);
    }

    private void collectTests(List<Path> testsPath) throws IOException {
        for (Path testPath : testsPath) {
            fileEntities.add(new FileEntity(testPath));
        }

        fileEntities.stream()
                .map(JdtAnalyzer::new)
                .forEach(e -> {
                    testMethodsMap.put(e.getFileEntity().getPath(), e.getTestMethods());
                    addImports(e.getImportDeclarations());
                });
    }

    public CompilationUnit unite() {
        //　テストを1つ適当に取ってくる
        JdtAnalyzer jdtAnalyzer = new JdtAnalyzer(fileEntities.get(0));
        CompilationUnit compilationUnit = jdtAnalyzer.getCompilationUnit();
        TypeDeclaration typeDeclaration = jdtAnalyzer.getTypeDeclaration();

        compilationUnit.accept(new ContentDeleteVisitor()); // テストメソッドとimportを一旦削除
        final JdtAnalyzer newJdtAnalyzer = new JdtAnalyzer(new FileEntity("", compilationUnit.toString()));
        CompilationUnit templateCompilationUnit = newJdtAnalyzer.getCompilationUnit();
        TypeDeclaration templateTypeDeclaration = newJdtAnalyzer.getTypeDeclaration();

        final Document document = new Document(templateCompilationUnit.toString());
        AST ast = templateTypeDeclaration.getAST();
        final ASTRewrite rewriter = ASTRewrite.create(ast);
        final ListRewrite listRewrite = rewriter.getListRewrite(templateTypeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        testMethodsMap.forEach((key, value) -> value.forEach(m -> {
            MethodDeclaration md = (MethodDeclaration) m;
            AST a = m.getAST();
            String newMethodName = CtppTestNameGenerator.generate(key, md.getName().getIdentifier());
            md.setName(a.newSimpleName(newMethodName));
            listRewrite.insertLast(m, null);
        }));

        final ListRewrite importListRewrite = rewriter.getListRewrite(templateCompilationUnit, CompilationUnit.IMPORTS_PROPERTY);
        importList.forEach(i -> importListRewrite.insertLast(i, null));

        TextEdit edits = rewriter.rewriteAST(document, null);
        try {
            edits.apply(document);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return new JdtAnalyzer(new FileEntity(path, document.get())).getCompilationUnit();
    }

    public CompilationUnit unite(String filename) {
        CompilationUnit compilationUnit = unite();
        if (filename != null) {
            String identifier = filename.split("\\.")[0];
            ((TypeDeclaration) compilationUnit.types().get(0)).getName().setIdentifier(identifier);
        }
        return compilationUnit;
    }

    public List<FileEntity> getFileEntities() {
        return fileEntities;
    }

    private void addImports(List<ImportDeclaration> imports) {
        for (ImportDeclaration id: imports) {
            boolean found = false;
            for (ImportDeclaration existId: importList) {
                if (id.getName().getFullyQualifiedName().equals(existId.getName().getFullyQualifiedName())) {
                    found = true;
                    break;
                }
            }
            if (!found)
                importList.add(id);
        }
    }

    class ContentDeleteVisitor extends ASTVisitor {

        @Override
        public boolean visit(MethodDeclaration node) {
            node.delete();
            return super.visit(node);
        }

        @Override
        public boolean visit(ImportDeclaration node) {
            node.delete();
            return super.visit(node);
        }

    }

}
