<%@ page contentType="text/html;charset=UTF-8" %>

<g:setProvider library="jquery"/>

<g:if test="${flash.message}">
  <div>${flash.message}</div>
</g:if>

<g:hasErrors bean="${cardInstance}">
  <div>
    <g:renderErrors bean="${cardInstance}" as="list" />
  </div>
</g:hasErrors>
<g:if test="${cardInstance?.id}">
  <g:formRemote url="${[controller:'card',action:'saveOrUpdate', params: [format: 'html']]}"
                update="editCardDialog" name="cardForm"
                onSuccess="cardFormRefresh(data,'#editCardDialog','Success', 'Card successfully updated')">

    <div class="header">
      <div class="assignee">
        <!-- Gravatar -->
      </div>
      <div class="info">

        <input type="text" id="card.title" name="title"
               class="property ${hasErrors(bean:cardInstance,field:'title','errors')}"
               value="${fieldValue(bean:cardInstance,field:'title')}" />

        <span class="date">Last updated: <g:formatDate format="yyyy-MM-dd HH:mm"
                                                       date="${cardInstance.lastUpdated}"/></span>

        <div class="caseNumberWrapper">
          <label for="caseNumber"><g:message code="_cardForm.label.caseNumber"/></label>
          <input type="text" id="card.caseNumber" name="caseNumber"
                 class="property ${hasErrors(bean:cardInstance,field:'caseNumber','errors')}"
                 value="${fieldValue(bean:cardInstance,field:'caseNumber')}" />
        </div>

      </div>
    </div>
    <div class="content">

      <label for="description"><g:message code="_cardForm.label.description"/></label>
      <textarea id="card.description" class="property ${hasErrors(bean:cardInstance,field:'description','errors')}"
                name="description">"${fieldValue(bean:cardInstance,field:'description')}"</textarea>


      <g:if test="${cardInstance.events}">
        <select name="events" size="4" multiple>
          <g:each in="${cardInstance.events}" var='event'>
            <option>${event.dateCreated}:
              ${event.user.username} moved to ${event.newPhase.name}, on CardIndex: ${event.newCardIndex}
            </option>
          </g:each>
        </select>
      </g:if>

    </div>

    <input type="hidden" name="id" value="${cardInstance?.id}"/>
    <input type="hidden" name="phase.id" value="${boardInstance.phases[0].id}" />
    <input style="display: none;" type="submit"/>
  </g:formRemote>

</g:if>


<g:else>
  <g:formRemote url="${[controller:'card',action:'saveOrUpdate', params: [format: 'html']]}"
                update="createCardDialog" name="cardForm"
                onSuccess="cardFormRefresh(data,'#createCardDialog')"
                before="if(\$('[name=title]').val() == 'Title...          ') \$('[name=title]').val('');">

    <div class="header">
      <div class="assignee">
        <!-- Gravatar -->
      </div>
      <div class="info">        
        <input type="text" id="card.title" name="title"
               class="property ${hasErrors(bean:cardInstance,field:'title','errors')}"
               value="Title...          " onfocus="if (this.value == 'Title...          ') this.value = '';"
               onblur="if (this.value == '') this.value = 'Title...          '"/>

       <span class="date"></span>
       <div class="caseNumberWrapper">
         <label for="caseNumber"><g:message code="_cardForm.label.caseNumber"/></label>
          <input type="text" id="card.caseNumber" name="caseNumber"
                 class="property ${hasErrors(bean:cardInstance,field:'caseNumber','errors')}"/>
        </div>

      </div>
    </div>
    <div class="content">
      <label for="description"><g:message code="_cardForm.label.description"/></label>
      <textarea id="card.description" class="property ${hasErrors(bean:cardInstance,field:'description','errors')}"
                name="description"></textarea>
    </div>
    
    <input type="hidden" name="phase.id" value="${boardInstance.phases[0].id}" />
    <input style="display: none;" type="submit"/>
           
  </g:formRemote>
</g:else>