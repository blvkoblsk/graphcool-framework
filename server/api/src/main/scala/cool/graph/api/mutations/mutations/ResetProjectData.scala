package cool.graph.api.mutations.mutations

import cool.graph.api.ApiDependencies
import cool.graph.api.database.mutactions.mutactions._
import cool.graph.api.database.mutactions.{MutactionGroup, Transaction}
import cool.graph.api.database.{DataItem, DataResolver}
import cool.graph.api.mutations.{ClientMutation, ReturnValue, ReturnValueResult}
import cool.graph.shared.models._

import scala.concurrent.Future

case class ResetProjectData(project: Project, dataResolver: DataResolver)(implicit apiDependencies: ApiDependencies)
  extends ClientMutation {

  override def prepareMutactions(): Future[List[MutactionGroup]] = {
    val disableChecks = List(DisableForeignKeyConstraintChecks())
    val removeRelations = project.relations.map(relation => TruncateTable(projectId = project.id,  tableName = relation.id))
    val removeDataItems = project.models.map(model => TruncateTable(projectId = project.id, tableName = model.name))
    val removeRelayIds = List(TruncateTable(projectId = project.id, tableName = "_RelayId"))
    val enableChecks = List(EnableForeignKeyConstraintChecks())

    val transactionMutaction = Transaction(disableChecks ++ removeRelations ++ removeDataItems ++ removeRelayIds ++ enableChecks, dataResolver)
    Future.successful(List(MutactionGroup(mutactions = List(transactionMutaction), async = false)))
  }

  override def getReturnValue: Future[ReturnValueResult] = Future.successful(ReturnValue(DataItem("", Map.empty)))
}