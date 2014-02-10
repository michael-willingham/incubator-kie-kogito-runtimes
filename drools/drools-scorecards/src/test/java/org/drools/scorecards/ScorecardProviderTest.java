package org.drools.scorecards;



import org.drools.compiler.compiler.ScoreCardFactory;
import org.drools.compiler.compiler.ScoreCardProvider;

import org.junit.Before;
import org.junit.Test;
import org.kie.api.definition.type.FactType;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.builder.ScoreCardConfiguration;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import java.io.InputStream;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertEquals;


public class ScorecardProviderTest {
    private static String drl;
    private ScoreCardProvider scoreCardProvider;

    @Before
    public void setUp() throws Exception {
        scoreCardProvider = ScoreCardFactory.getScoreCardProvider();
        assertNotNull(scoreCardProvider);
    }

    @Test
    public void testDrlWithoutSheetName() throws Exception {
        InputStream is = ScorecardProviderTest.class.getResourceAsStream("/scoremodel_c.xls");
        assertNotNull(is);

        ScoreCardConfiguration scconf = KnowledgeBuilderFactory.newScoreCardConfiguration();
        drl = scoreCardProvider.loadFromInputStream(is, scconf);
        assertNotNull(drl);
        assertTrue(drl.length() > 0);
        //System.out.println(drl);
    }

    @Test
    public void testDrlWithSheetName() throws Exception {
        InputStream is = ScorecardProviderTest.class.getResourceAsStream("/scoremodel_c.xls");
        assertNotNull(is);

        ScoreCardConfiguration scconf = KnowledgeBuilderFactory.newScoreCardConfiguration();
        scconf.setWorksheetName( "scorecards" );
        drl = scoreCardProvider.loadFromInputStream(is, scconf);
        assertNotNull(drl);
        assertTrue(drl.length() > 0);
    }

    @Test
    public void testKnowledgeBaseWithExection() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        ScoreCardConfiguration scconf = KnowledgeBuilderFactory.newScoreCardConfiguration();
        scconf.setWorksheetName( "scorecards" );
        kbuilder.add( ResourceFactory.newUrlResource(ScorecardProviderTest.class.getResource("/scoremodel_c.xls")),
            ResourceType.SCARD,
            scconf );
        assertFalse( kbuilder.hasErrors() );
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        assertNotNull(kbase);
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        //NEW WORKING MEMORY
        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
        FactType scorecardType = kbase.getFactType( "org.drools.scorecards.example","SampleScore" );
        assertNotNull(scorecardType);

        Object scorecard =  scorecardType.newInstance();
        assertNotNull(scorecard);

        scorecardType.set(scorecard, "age", 10);
        session.insert( scorecard );
        session.fireAllRules();
        session.dispose();
        //occupation = 5, age = 25, validLicence -1
        assertEquals( 29.0, scorecardType.get( scorecard, "scorecard__calculatedScore" ) );

    }

    @Test
    public void testDrlGenerationWithExternalTypes() throws Exception {

        InputStream is = ScorecardProviderTest.class.getResourceAsStream("/scoremodel_externalmodel.xls");
        assertNotNull(is);

        ScoreCardConfiguration scconf = KnowledgeBuilderFactory.newScoreCardConfiguration();
        scconf.setWorksheetName( "scorecards" );
        scconf.setUsingExternalTypes(true);
        String drl = scoreCardProvider.loadFromInputStream(is, scconf);
        assertNotNull(drl);
        assertTrue(drl.length() > 0);

        assertTrue(drl.contains("org.drools.scorecards.example.Applicant"));

    }
}