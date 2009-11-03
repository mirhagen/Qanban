package se.qbranch.qanban

import grails.test.*
import grails.converters.*

class MainViewControllerTests extends ControllerUnitTestCase {

    def b

    protected void setUp() {
        super.setUp()
        mockDomain(Card, [ new Card(title: "TestCard",
                                    description: "This is a description",
                                    caseNumber: 1),

                           new Card(title: "OtherCard",
                                    description: "This is the other card",
                                    caseNumber: 2),
                           new Card(title: "Card three",
                                    description: "This is the third card",
                                    caseNumber: 5)])
  
        mockDomain(Board)
        mockDomain(Phase)

        b = new Board().addToPhases(new Phase(name: "test"))
        .addToPhases(new Phase(name: "other phase"))
        .addToPhases(new Phase(name: "thid phase"))
        .save()
        for(card in Card.list()) {
            def phase = b.phases[0]
            def p2 = b.phases[1]
            def p3 = b.phases[2]
            phase.cards.add(card)
            card.phase = phase
            card.phase.board = b
            p2.board = b
            p3.board = b
            p2.save()
            p3.save()
            phase.save()
            card.save()
        }



    }

    void testSetup() {
        assertEquals 1,Board.list().size()
        assertEquals 3,Phase.list().size()
        assertEquals 3,Card.list().size()

    }

    protected void tearDown() {
        super.tearDown()
    }

    void testMoveCardSuccess() {

        // Ta card med id 3 och flytta från pos 2 till pos 0
        // om det funkar får man true och domänmodellen ser annorlunda ut, verifiera

        // Making request to moveCard
        mockParams.id = "3"
        mockParams.moveTo = "0"
        controller.moveCard()
        def response = JSON.parse(controller.response.contentAsString)
        assertTrue "Expected move to return true", response.result

        assertEquals 3, b.phases[0].cards[0].id
        assertEquals 3, b.phases[0].cards.size()

        
    }

    void testMoveCardToIllegalPosition() {
        mockParams.id = 3
        mockParams.moveTo = 3
        controller.moveCard()
        def response = JSON.parse(controller.response.contentAsString)
        assertFalse "Expected move to return false", response.result

    }

    void testNotSettingMoveTo() {
        mockParams.id = 3
        controller.moveCard()
        def response = JSON.parse(controller.response.contentAsString)
        assertFalse "Expected move to return false", response.result
    }

    void testMoveCardToAllowedPhase() {
        mockParams.id = 3
        mockParams.moveTo = 2
        controller.moveCardToPhase()
        def response = JSON.parse(controller.response.contentAsString)
        assertTrue "Expected move to return true", response.result
        assertEquals 3, b.phases[1].cards[0].id
    }

    void testMoveCardToPhaseThatDoesntExists() {
        mockParams.id = 3
        mockParams.moveTo = 7
        controller.moveCardToPhase()
        def response = JSON.parse(controller.response.contentAsString)
        assertFalse "Expected move to return false", response.result
    }

    void testMoveCardMoreThanOnePhase() {
        mockParams.id = 3
        mockParams.moveTo = 3
        controller.moveCardToPhase()
        def response = JSON.parse(controller.response.contentAsString)
        assertFalse "Expected move to return false", response.result
    }

    void testMoveCardBackwards() {
        mockParams.id = 3
        mockParams.moveTo = 3
        controller.moveCardToPhase()
        def response = JSON.parse(controller.response.contentAsString)
        assertFalse "Expected move to return false", response.result
    }

    void testNotSettingPhaseMoveTo() {
        mockParams.id = 3
        controller.moveCardToPhase()
        def response = JSON.parse(controller.response.contentAsString)
        assertFalse "Expected move to return false", response.result
    }
}
