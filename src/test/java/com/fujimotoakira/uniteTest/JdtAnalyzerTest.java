package com.fujimotoakira.uniteTest;

import com.fujimotoakira.uniteTest.io.FileEntity;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdtAnalyzerTest {

    @Test
    public void testCreateTree() throws IOException {
        // クラスローダでファイルの位置を指定する
        String path = JdtAnalyzerTest.class.getClassLoader().getResource("exampleTests/Example01Test.java").getPath();
        FileEntity fileEntity = new FileEntity(path);
        JdtAnalyzer analyzer = new JdtAnalyzer(fileEntity);
        final CompilationUnit compilationUnit = analyzer.getCompilationUnit();
        assertEquals(compilationUnit.getNodeType(), ASTNode.COMPILATION_UNIT);
    }

    @Test
    public void testGetMethods() throws IOException {
        String path = JdtAnalyzerTest.class.getClassLoader().getResource("exampleTests/Example01Test.java").getPath();
        FileEntity fileEntity = new FileEntity(path);
        JdtAnalyzer analyzer = new JdtAnalyzer(fileEntity);
        final List<ASTNode> testMethods = analyzer.getTestMethods();
        assertEquals(testMethods.size(), 3);
        assertEquals(testMethods.get(0).getNodeType(), ASTNode.METHOD_DECLARATION);
        assertEquals(((MethodDeclaration) testMethods.get(0)).getName().toString(), "test01");
    }

}