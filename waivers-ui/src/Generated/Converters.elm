module Generated.Converters exposing (posixToIsoString, dateToIsoString, boolToString, dateDecoder, dateEncoder)

import Time exposing (Posix)
import Date exposing (Date)
import Json.Decode as Decode exposing (Decoder)
import Json.Encode as Encode


boolToString : Bool -> String
boolToString value =
    if value then
        "true"

    else
        "false"


dateToIsoString : Date -> String
dateToIsoString date =
    Date.toIsoString date


posixToIsoString : Posix -> String
posixToIsoString posix =
    dateToIsoString (Date.fromPosix Time.utc posix)


monthToInt : Time.Month -> Int
monthToInt month =
    case month of
        Time.Jan -> 1
        Time.Feb -> 2
        Time.Mar -> 3
        Time.Apr -> 4
        Time.May -> 5
        Time.Jun -> 6
        Time.Jul -> 7
        Time.Aug -> 8
        Time.Sep -> 9
        Time.Oct -> 10
        Time.Nov -> 11
        Time.Dec -> 12


dateDecoder : Decoder Date
dateDecoder =
    Decode.string
        |> Decode.andThen
            (\str ->
                case Date.fromIsoString str of
                    Err e ->
                        Decode.fail e

                    Ok date ->
                        Decode.succeed date
            )


dateEncoder : Date -> Encode.Value
dateEncoder date =
    Encode.string (dateToIsoString date)
