module ValidationError exposing
    ( ValidationError
    , validationErrorsDecoder
    )

import Json.Decode as Decode exposing (Decoder, list, nullable, string)
import Json.Decode.Pipeline exposing (optional, required)


type alias ValidationError =
    { message : String
    , field : Maybe String
    }


validationErrorDecoder : Decoder ValidationError
validationErrorDecoder =
    Decode.succeed ValidationError
        |> required "message" string
        |> optional "field" (nullable string) Nothing


validationErrorsDecoder : Decoder (List ValidationError)
validationErrorsDecoder =
    list validationErrorDecoder