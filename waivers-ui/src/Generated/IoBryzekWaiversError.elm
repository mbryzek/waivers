module Generated.IoBryzekWaiversError exposing (..)

import Json.Decode as Decode
import Json.Decode.Pipeline as Pipeline
import Json.Encode as Encode
import Http exposing (Header)




encodeOptional : (a -> Encode.Value) -> Maybe a -> Encode.Value
encodeOptional encoder value =
    case value of
        Just v ->
            encoder v

        Nothing ->
            Encode.null


type alias HttpRequestParams =
     { apiHost: String
       , headers : List Header
     }


type alias GenericError =
  {
    code: String
    , message: String
  }

genericErrorEncoder : GenericError -> Encode.Value
genericErrorEncoder instance =
        Encode.object
        [
            ( "code", Encode.string instance.code )
            , ( "message", Encode.string instance.message )

        ]


genericErrorDecoder : Decode.Decoder GenericError
genericErrorDecoder =
        Decode.succeed GenericError
            |> Pipeline.required "code" Decode.string
            |> Pipeline.required "message" Decode.string