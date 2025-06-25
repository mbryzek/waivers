module Generated.IoBryzekWaiversAdmin exposing (..)

import Generated.ApiRequest as ApiRequest
import Generated.IoBryzekWaiversApi as IoBryzekWaiversApi
import Json.Decode as Decode
import Json.Decode.Pipeline as Pipeline
import Json.Encode as Encode
import Generated.ApiRequest exposing (ApiResult)
import Http exposing (Header)
import Url.Builder exposing (string, toQuery)




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


type alias ProjectForm =
  {
    name: String
    , slug: String
    , description: Maybe String
    , waiverTemplate: String
    , status: IoBryzekWaiversApi.ProjectStatus
  }

projectFormEncoder : ProjectForm -> Encode.Value
projectFormEncoder instance =
        Encode.object
        [
            ( "name", Encode.string instance.name )
            , ( "slug", Encode.string instance.slug )
            , ( "description", encodeOptional Encode.string instance.description )
            , ( "waiver_template", Encode.string instance.waiverTemplate )
            , ( "status", IoBryzekWaiversApi.projectStatusEncoder instance.status )

        ]


projectFormDecoder : Decode.Decoder ProjectForm
projectFormDecoder =
        Decode.succeed ProjectForm
            |> Pipeline.required "name" Decode.string
            |> Pipeline.required "slug" Decode.string
            |> Pipeline.optional "description" (Decode.nullable Decode.string) Nothing
            |> Pipeline.required "waiver_template" Decode.string
            |> Pipeline.required "status" IoBryzekWaiversApi.projectStatusDecoder


type alias GetProjectFormsProjectsProps =
    {limit : Int
    , offset : Int
    }

getProjectFormsProjects : GetProjectFormsProjectsProps -> (ApiResult (ApiRequest.PaginatedList IoBryzekWaiversApi.Project) -> msg) -> HttpRequestParams -> Cmd msg
getProjectFormsProjects props msg params =
    Http.request
        { method = "GET"
        , url = params.apiHost ++ String.append "/project_forms/projects" (toQuery(
            [ string "limit" (String.fromInt (props.limit + 1)) ]
                    ++ [ string "offset" (String.fromInt props.offset) ]
            ))
        , expect = ApiRequest.expectJson msg (Decode.map (ApiRequest.decodePagination props.limit props.offset) (Decode.list IoBryzekWaiversApi.projectDecoder))
        , headers = params.headers
        , timeout = Nothing
        , tracker = Nothing
        , body = Http.emptyBody
        }