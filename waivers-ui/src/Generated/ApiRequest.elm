module Generated.ApiRequest exposing (ApiError(..), ApiRequest(..), ApiResult, PaginatedList, map, mapPaginatedList, expectJson, expectUnit, toMaybe, fromResult, decodePagination, isLoading, isSuccess, makeValidationErrorRequest, makeValidationErrorsRequest)

import Http
import Json.Decode as Decode exposing (Decoder, decodeString, errorToString)
import ValidationError exposing (ValidationError, validationErrorsDecoder)


type ApiRequest a
    = NotAsked
    | Loading
    | Failure ApiError
    | Success a


type alias ApiResult a =
    Result ApiError a


type ApiError
    = ApiErrorSystem String
    | ApiErrorUnsupportedStatusCode Int
    | ApiErrorJsonParse String
    | ApiErrorNotFound
    | ApiErrorNotAuthorized
    | ApiErrorValidation (List ValidationError)


mapApiResponse : Http.Response String -> (String -> ApiResult a) -> ApiResult a
mapApiResponse httpResponse f =
    case httpResponse of
        Http.BadUrl_ url ->
            Err (ApiErrorSystem (String.append "Bad URL: " url))

        Http.Timeout_ ->
            Err (ApiErrorSystem "Timeout")

        Http.NetworkError_ ->
            Err (ApiErrorSystem "NetworkError")

        Http.BadStatus_ metadata body ->
            case metadata.statusCode of
                401 ->
                    Err ApiErrorNotAuthorized

                404 ->
                    Err ApiErrorNotFound

                422 ->
                    case decodeString validationErrorsDecoder body of
                        Ok errors ->
                            Err (ApiErrorValidation errors)

                        Err e ->
                            Err (ApiErrorSystem ("422 - unable to parse body as validation error: " ++ errorToString e))

                code ->
                    Err (ApiErrorUnsupportedStatusCode code)

        Http.GoodStatus_ _ body ->
            case f body of
                Ok obj ->
                    Ok obj

                Err e ->
                    Err e

expectJson : (ApiResult a -> msg) -> Decode.Decoder a  -> Http.Expect msg
expectJson msg decoder =
    Http.expectStringResponse msg (convertJson decoder)


expectUnit : (ApiResult () -> msg) -> Http.Expect msg
expectUnit msg =
    Http.expectStringResponse msg convertUnit


convertJson : Decoder a -> Http.Response String -> ApiResult a
convertJson decoder httpResponse =
    mapApiResponse httpResponse
        (\body ->
            case decodeString decoder body of
                Ok obj ->
                    Ok obj

                Err e ->
                    Err (ApiErrorJsonParse (errorToString e))
        )


convertUnit : Http.Response String -> ApiResult ()
convertUnit httpResponse =
    mapApiResponse httpResponse (\_ -> Ok ())

map : (a -> b) -> ApiRequest a -> ApiRequest b
map f request =
    case request of
        Success value ->
            Success (f value)

        Loading ->
            Loading

        NotAsked ->
            NotAsked

        Failure error ->
            Failure error


mapPaginatedList : (a -> a) -> ApiRequest (PaginatedList a) -> ApiRequest (PaginatedList a)
mapPaginatedList f request =
    map (\paginatedList ->
        { paginatedList | records = List.map f paginatedList.records }
    ) request


toMaybe : ApiRequest a -> Maybe a
toMaybe request =
    case request of
        Success value ->
            Just value

        _ ->
            Nothing

fromResult : ApiResult a -> ApiRequest a
fromResult result =
    case result of
        Ok value -> Success value
        Err error -> Failure error

type alias PaginatedList a =
    {
        records: List a
        , limit : Int
        , offset : Int
        , previousOffset : Maybe Int
        , nextOffset : Maybe Int
    }


decodePagination : Int -> Int -> List a -> PaginatedList a
decodePagination limit offset records =
    let
        previousOffset : Maybe Int
        previousOffset =
            let
                n : Int
                n = offset - limit
            in
            if offset > 0 then
                if n > 0 then
                    Just n
                else
                    Just 0
            else
                Nothing

        nextOffset : Maybe Int
        nextOffset =
            if List.length records > limit then
                Just (offset + limit)
            else
                Nothing
    in
    {
        records = List.take limit records
        , limit = limit
        , offset = offset
        , previousOffset = previousOffset
        , nextOffset = nextOffset
    }


isLoading : ApiRequest a -> Bool
isLoading request =
    case request of
        Loading -> True
        _ -> False


isSuccess : ApiRequest a -> Bool
isSuccess request =
    case request of
        Success _ -> True
        _ -> False


makeValidationErrorRequest : String -> ApiRequest a
makeValidationErrorRequest message =
    makeValidationErrorsRequest [ message ]


makeValidationErrorsRequest : List String -> ApiRequest a
makeValidationErrorsRequest errors =
    Failure (ApiErrorValidation (List.map (\message -> { message = message, field = Nothing }) errors))
