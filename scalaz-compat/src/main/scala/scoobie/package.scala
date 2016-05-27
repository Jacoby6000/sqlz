import scalaz.concurrent.Task

/**
  * Created by jacob.barber on 5/27/16.
  */
package object scoobie {
  implicit class TaskCompatExtensions[A](task: Task[A]) {
    def unsafePerformSync: A = task.run
  }
}
