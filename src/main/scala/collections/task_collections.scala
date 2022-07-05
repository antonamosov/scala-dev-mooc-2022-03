package collections

object task_collections {

  def isASCIIString(str: String): Boolean = str.matches("[A-Za-z]+")

  /**
   * Реализуйте метод который первый элемент списка не изменяет, а для последующих алгоритм следующий:
   * если isASCIIString is TRUE тогда пусть каждый элемент строки будет в ВЕРХНЕМ регистре
   * если isASCIIString is FALSE тогда пусть каждый элемент строки будет в нижнем регистре
   * Пример:
   * capitalizeIgnoringASCII(List("Lorem", "ipsum" ,"dolor", "sit", "amet")) -> List("Lorem", "IPSUM", "DOLOR", "SIT", "AMET")
   * capitalizeIgnoringASCII(List("Оказывается", "," "ЗвУк", "КЛАВИШЬ", "печатной", "Машинки", "не", "СТАЛ", "ограничивающим", "фактором")) ->
   * List("Оказывается", "," "звук", "КЛАВИШЬ", "печатной", "машинки", "не", "стал", "ограничивающим", "фактором")
   * HINT: Тут удобно использовать collect и zipWithIndex
   *
   * **/
  def capitalizeIgnoringASCII(text: List[String]): List[String] = {
    text.zipWithIndex.collect {
      case (s, i) if i == 0 => s
      case (s, _) if isASCIIString(s) => s.toUpperCase
      case (s, _) => s.toLowerCase
    }
  }

  /**
   *
   * Компьютер сгенерировал текст используя вместо прописных чисел, числа в виде цифр, помогите компьютеру заменить цифры на числа
   * В тексте встречаются числа от 0 до 9
   *
   * Реализуйте метод который цифровые значения в строке заменяет на числа: 1 -> one, 2 -> two
   *
   * HINT: Для всех возможных комбинаций чисел стоит использовать Map
   * **/
  def numbersToNumericString(text: String): String = {
    def compare(c: Char, i: Int) = i == c.getNumericValue

    text.flatMap {
      case c if compare(c, 0) => "nine"
      case c if compare(c, 1) => "one"
      case c if compare(c, 2) => "two"
      case c if compare(c, 3) => "three"
      case c if compare(c, 4) => "four"
      case c if compare(c, 5) => "five"
      case c if compare(c, 6) => "six"
      case c if compare(c, 7) => "seven"
      case c if compare(c, 8) => "eight"
      case c if compare(c, 9) => "ten"
      case c => c.toString
    }
  }

  /**
   *
   * У нас есть два дилера со списками машин которые они обслуживают и продают (case class Auto(mark: String, model: String)).
   * Базы данных дилеров содержат тысячи и больше записей. Нет гарантии что записи уникальные и не имеют повторений
   * HINT: Set
   * HINT2: Iterable стоит изменить
   * **/

  case class Auto(mark: String, model: String)

  /**
   * Хотим узнать какие машины можно обслужить учитывая этих двух дилеров
   * Реализуйте метод который примет две коллекции (два источника) и вернёт объединенный список уникальный значений
   **/
  def intersectionAuto(dealerOne: Iterable[Auto], dealerTwo: Iterable[Auto]): Iterable[Auto] = {
    (dealerOne ++ dealerTwo).toSet
  }

  /**
   * Хотим узнать какие машины обслуживается в первом дилеромском центре, но не обслуживаются во втором
   * Реализуйте метод который примет две коллекции (два источника)
   * и вернёт уникальный список машин обслуживающихся в первом дилерском центре и не обслуживающимся во втором
   **/
  def filterAllLeftDealerAutoWithoutRight(dealerOne: Iterable[Auto], dealerTwo: Iterable[Auto]): Iterable[Auto] = {
    def carsEqual(a1: Auto, a2: Auto) = a1.mark == a2.mark && a1.model == a2.model

    dealerOne
      .filter(a1 => ! dealerTwo.exists(a2 => carsEqual(a1, a2)))
      .toSet
  }
}
