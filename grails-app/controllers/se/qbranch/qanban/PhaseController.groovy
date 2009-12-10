/*
 * Copyright 2009 Qbranch AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.qbranch.qanban

import grails.converters.*
import org.codehaus.groovy.grails.plugins.springsecurity.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class PhaseController {

    def securityService
    def eventService

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    // Create

    @Secured(['ROLE_QANBANADMIN'])
    def create = {
    
        if( !params.'board.id' )
            return render(status: 400, text: "The parameter 'boardId' must be specified")

        def createEvent = createPhaseEventCreate(params);
        eventService.persist(createEvent)

        renderCreateResult(createEvent)

    }

    private PhaseEventCreate createPhaseEventCreate(params){
        def event = new PhaseEventCreate(params)
        event.user = securityService.getLoggedInUser()
        return event
    }

    private renderCreateResult(createEvent){
        withFormat{
            html{
                def board = createEvent.board
                return render(template:'phaseForm',model:[createEvent:createEvent, boardInstance: board])
            }
            js{
                return render ( [ phaseInstance : createEvent.phase] as JSON)
            }
            xml{
                return render ( [ phaseInstance : createEvent.phase ] as XML)
            }
        }
    }

    // Retrieve

    def show = {

        if( !params.id )
            return render(status: 400, text: "You need to specify an id")

        if( !Phase.exists(params.id) )
            return render(status: 404, text: "Phase with id $params.id not found")

        def phase = Phase.get(params.id)

        renderShowResult(phase)
        
    }

    private renderShowResult(phase){
        withFormat {
            html{
                return render (template: 'phase', model:[phase:phase])
            }
            js{
                return render ( [phaseInstance : phase] as JSON )
            }
            xml{
                return render ( [phaseInstance : phase] as XML )
            }
        }
    }

    @Secured(['ROLE_QANBANADMIN'])
    def form = {

        if( !params.id )
            return renderFormCreateMode(params)

        if( !Phase.exists(params.id) )
            return render(status: 404, text: "Phase with id $params.id not found")

        return renderFormEditMode(params)

    }


    private def renderFormCreateMode(params){
        if ( !params.'board.id' )
            return render(status: 400, text: "The parameter 'boardId' must be specified")
        def board = Board.get(params.'board.id')
        return render(template:'phaseForm',model:[createEvent: new PhaseEventCreate(), boardInstance: board ])
    }

    private def renderFormEditMode(params){
        def updateEvent = new PhaseEventUpdate()
        updateEvent.phase = Phase.get(params.id)
        return render(template:'phaseForm',model:[ updateEvent: updateEvent ])
    }

    // Update
    
    // TODO: Fixa så att eventen endast sparas när man ändrar nåt samt flytta
    @Secured(['ROLE_QANBANADMIN'])
    def update = { MovePhaseCommand cmd ->

        def updateEvent = createUpdateEvent(params)

        if ( !cmd.hasErrors() && updateEvent.validate() ){
            
            def moveEvent = createPhaseEventMove(cmd)
            if( moveEvent )
                eventService.persist(moveEvent)
            eventService.persist(updateEvent)
            
        }

        renderUpdateResult(updateEvent)

    }

    private renderUpdateResult(updateEvent){
        withFormat{
            html{
                return render(template:'phaseForm',model:[ updateEvent:updateEvent ])
            }
            js{
                return render ( [ phaseInstance : updateEvent.phase ] as JSON)
            }
            xml{
                return render ( [ phaseInstance : updateEvent.phase ] as XML)
            }
        }
    }

    private PhaseEventUpdate createUpdateEvent(params){
        def event = new PhaseEventUpdate()
        event.phase = Phase.get(params.id)
        event.properties = params
        event.user = securityService.getLoggedInUser()
        return event
    }


    private PhaseEventMove createPhaseEventMove(cmd){
        if(phaseIsMovedToANewPosition(cmd)){
            def moveEvent = new PhaseEventMove(
                phase: cmd.phase,
                phasePos: cmd.phasePos,
                user: securityService.getLoggedInUser()
            )
            return moveEvent
        }
    }

    private boolean phaseIsMovedToANewPosition(cmd){
        return cmd.phasePos != cmd.phase.board.phases.indexOf(cmd.phase)
    }


    // Delete

    @Secured(['ROLE_QANBANADMIN'])
    def delete = {
        
        if( !params.id )
            return render(status: 400, text: "You need to specify a phase")
        if( !Phase.exists(params.id) )
            return render(status: 404, text: "Phase with id $params.id not found")

        def deleteEvent = new PhaseEventDelete()
        deleteEvent.user = securityService.getLoggedInUser()
        deleteEvent.phase = Phase.get( params.id )

        eventService.persist(deleteEvent)

        if( !deleteEvent.hasErrors() )
            return render(status: 200, text: "Phase with id $params.id deleted")

        return render(status: 503, text: "Server error: phase delete error #186")

    }


    /****
     *  Temporary ajax call actions
     ****/


    @Secured(['ROLE_QANBANADMIN'])
    def movePhase = { MovePhaseCommand cmd ->

        if( cmd.hasErrors() ){
            return render([result: false] as JSON)
        }else{
            def moveEvent = createPhaseEventMove(cmd)

            render "Phase $moveEvent.phase.title Id $moveEvent.id"


        }

    }

}

class MovePhaseCommand {

    static constraints = {

        id( min: 0, nullable: false, validator:{ val, obj ->
                Phase.exists(val)
            })
        phasePos( min: 0, nullable: false, validator:{ val, obj ->
                
                return ( val < obj.phase.board.phases.size() )
             
            })
    }

    Integer id
    Integer phasePos

    def getPhase() {
        Phase.get(id)
        
    }
}
