package scoobie.connection.postgres.protocol

import scalaz.{ImmutableArray, IMap}

object messages {
  sealed trait Message
  sealed trait AuthRequest
  sealed trait AuthResponse
  case object Ok extends AuthResponse
  case object KerebrosV5 extends AuthResponse
  case object CleartextPassword extends AuthResponse
  case object MD5Password extends AuthResponse
  case object SCMCredential extends AuthResponse
  case object AuthenticationGSS extends AuthResponse
  case object AuthenticationSSPI extends AuthResponse
  case object AuthenticationGSSContinue extends AuthResponse
  case class GSSContinue(authData: Any) extends AuthResponse


  case class BackendKeyData(processId: Int, secretKey: Int)

  sealed trait FormatCode
  case object Binary extends FormatCode
  case object Text extends FormatCode

  sealed trait Param
  case class TextParam(text: String)
  case class Bin(arr: ImmutableArray[Byte])

  case class Bind(destination: String, source: String, params: ImmutableArray[Param], resultColumnFormat: ImmutableArray[FormatCode])
  case object BindComplete


  case class CancelRequest(processId: Int, secretKey: String)

  sealed trait Close
  case class ClosePreparedStatement(name: String) extends Close
  case class ClosePortal(name: String) extends Close

  case object CloseComplete

  sealed trait CommandComplete
  case class InsertComplete(oid: Int, rows: Int) extends CommandComplete
  case class DeleteComplete(rows: Int) extends CommandComplete
  case class UpdateComplete(rows: Int) extends CommandComplete
  case class SelectComplete(rows: Int) extends CommandComplete
  case class MoveComplete(rows: Int) extends CommandComplete
  case class FetchComplete(rows: Int) extends CommandComplete
  case class CopyComplete(rows: Int) extends CommandComplete

  case class CopyData(bytes: ImmutableArray[Byte])

  case object CopyDone

  case class CopyFail(message: String)


  sealed trait CopyResponseType
  case object In extends CopyResponseType
  case object Out extends CopyResponseType
  case object Both extends CopyResponseType

  case class CopyResponse(copyType: CopyResponseType, overallFormatCode: FormatCode, columnFormatCodes: ImmutableArray[FormatCode])

  case class DataRow(columns: ImmutableArray[Param])

  sealed trait Describe
  case class DescribePreparedStatement(name: String) extends Describe
  case class DescribePortal(name: String) extends Describe

  case object EmptyQueryResponse

  sealed trait FieldType
  case object NonLocalizedSeverity extends FieldType
  case object Severity extends FieldType
  case object Code extends FieldType
  case object Message extends FieldType
  case object Detail extends FieldType
  case object Hint extends FieldType
  case object Position extends FieldType
  case object InternalPosition extends FieldType
  case object InternalQuery extends FieldType
  case object Where extends FieldType
  case object SchemaName extends FieldType
  case object TableName extends FieldType
  case object ColumnName extends FieldType
  case object DataTypeName extends FieldType
  case object ConstraintName extends FieldType
  case object File extends FieldType
  case object Line extends FieldType
  case object Routine extends FieldType

  case class ErrorResponse(fields: IMap[FieldType, String])

  case class Execute(name: String, limit: Int)

  case object Flush

  case class FunctionCall(oid: Int, args: ImmutableArray[Param], resultType: FormatCode)
  case class FunctionCallResponse(result: Param)
  case class GSSResponse(data: Any)
  case object NoData
  case class NoticeResponse(fields: IMap[FieldType, String])

  case class NotificationResponse(processId: Int, channelName: String, payload: String)
  case class ParameterDescription(paramIds: ImmutableArray[Int])
  case class ParameterStatus(parameterName: String, value: String)
  case class Parse(destination: String, query: String, paramIds: ImmutableArray[Int])
  case object ParseComplete
  case class PasswordMessage(password: String)
  case object PortalSuspended
  case class Query(query: String)

  sealed trait TransactionStatus
  case object Idle extends TransactionStatus
  case object Active extends TransactionStatus
  case object Error extends TransactionStatus

  case class ReadyForQuery(status: TransactionStatus)

  case class RowDescription(fields: ImmutableArray[RowDescriptionField])
  case class RowDescriptionField(name: String, tableId: Int, tableColumn: Int, typeId: Int, dataTypeSize: Int, typeModifier: Int, formatCode: FormatCode)

  case object SSLRequest

  sealed trait StartupField
  case object User extends StartupField
  case object Database extends StartupField
  case object Options extends StartupField
  case class RuntimeParam(name: String) extends StartupField

  case class StartupMessage(protocolVersion: Int, params: IMap[StartupField, String])

  case object Sync
  case object Terminate

}
